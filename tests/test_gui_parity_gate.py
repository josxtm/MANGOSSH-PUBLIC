import sys
import tempfile
from pathlib import Path
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools"))
from gui_parity_gate import check, check_launcher


class PublicGuiParityTest(unittest.TestCase):
    def test_real_manifest_launches_compose(self):
        self.assertEqual(check_launcher(Path(__file__).resolve().parents[1]), [])

    def test_generic_java_launcher_is_blocked(self):
        with tempfile.TemporaryDirectory() as root:
            target = Path(root) / "app/src/main/AndroidManifest.xml"
            target.parent.mkdir(parents=True)
            target.write_text(
                '<manifest xmlns:android="http://schemas.android.com/apk/res/android">'
                '<application><activity android:name=".MainActivity" android:exported="true">'
                '<intent-filter><action android:name="android.intent.action.MAIN"/>'
                '<category android:name="android.intent.category.LAUNCHER"/>'
                '</intent-filter></activity></application></manifest>', encoding="utf-8")
            self.assertTrue(check_launcher(root))

    def test_missing_component_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            self.assertTrue(check(root, {"ui/Screen.kt": ["Scaffold"]}))

    def test_placeholder_is_rejected(self):
        with tempfile.TemporaryDirectory() as root:
            candidate = Path(root) / "ui/Screen.kt"
            candidate.parent.mkdir()
            candidate.write_text("class Placeholder", encoding="utf-8")
            self.assertTrue(check(root, {"ui/Screen.kt": ["Scaffold"]}))

    def test_structural_landmark_present(self):
        with tempfile.TemporaryDirectory() as root:
            candidate = Path(root) / "ui/Screen.kt"
            candidate.parent.mkdir()
            candidate.write_text("Scaffold originalScreen", encoding="utf-8")
            self.assertEqual(check(root, {"ui/Screen.kt": ["Scaffold"]}), [])


if __name__ == "__main__":
    unittest.main()
