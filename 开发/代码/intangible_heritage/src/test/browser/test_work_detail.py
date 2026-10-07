import unittest
from pathlib import Path

from playwright.sync_api import sync_playwright


TEMPLATE = Path(__file__).resolve().parents[2] / "main/resources/templates/work/detail.html"


class WorkDetailBrowserTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.playwright = sync_playwright().start()
        cls.browser = cls.playwright.chromium.launch(headless=True)

    @classmethod
    def tearDownClass(cls):
        cls.browser.close()
        cls.playwright.stop()

    def setUp(self):
        self.page = self.browser.new_page()
        self.page.set_default_timeout(5000)
        self.page.route("**/*", lambda route: route.abort())
        self.page.set_content(TEMPLATE.read_text(encoding="utf-8"))

    def tearDown(self):
        self.page.close()

    def test_progress_is_readable_chinese(self):
        self.page.evaluate("setModelProgress(42)")
        self.assertEqual(self.page.locator("#model-loading-text").text_content(), "已加载 42%")

    def prepare_animation(self):
        self.page.evaluate("""() => {
            window.renderCount = 0;
            modelViewer.renderer = {render() { window.renderCount++; }};
            modelViewer.controls = {update() {}};
            modelViewer.root = {};
            ensureModelViewer = async function () { animateModel(); };
            setMode('model');
        }""")
        self.page.wait_for_function("window.renderCount > 2")

    def test_switch_to_photo_pauses_and_return_resumes(self):
        self.prepare_animation()
        self.page.evaluate("setMode('photo')")
        count = self.page.evaluate("window.renderCount")
        self.page.wait_for_timeout(100)
        self.assertEqual(count, self.page.evaluate("window.renderCount"))
        self.page.evaluate("setMode('model')")
        self.page.wait_for_function("window.renderCount > " + str(count))

    def test_hidden_document_pauses_rendering(self):
        self.prepare_animation()
        self.page.evaluate("""() => {
            Object.defineProperty(document, 'hidden', {value: true, configurable: true});
            document.dispatchEvent(new Event('visibilitychange'));
        }""")
        count = self.page.evaluate("window.renderCount")
        self.page.wait_for_timeout(100)
        self.assertEqual(count, self.page.evaluate("window.renderCount"))

    def test_broken_image_has_message_and_next_image_recovers(self):
        self.page.evaluate("document.getElementById('main-photo').src = 'https://invalid.test/broken.png'")
        self.page.locator("#photo-error").wait_for(state="visible")
        self.assertTrue(self.page.locator("#hero-button").is_disabled())
        self.page.evaluate("""() => {
            const button = document.querySelector('.thumb');
            button.dataset.thumb = '0';
            button.dataset.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';
            button.dataset.label = '图片 1';
            selectPhoto(button);
        }""")
        self.page.locator("#photo-error").wait_for(state="hidden")
        self.page.wait_for_function("document.getElementById('main-photo').naturalWidth > 0")
        self.assertFalse(self.page.locator("#hero-button").is_disabled())


if __name__ == "__main__":
    unittest.main()
