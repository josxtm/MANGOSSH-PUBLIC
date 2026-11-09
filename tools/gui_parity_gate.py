#!/usr/bin/env python3
"""Blocks APK publication when public GUI cannot structurally resemble the approved GUI.

This check does not compare screenshots and is not proof of full visual parity.
Public files only; never loads private repositories.
"""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

REQUIRED = {
    "app/src/main/java/io/github/josxtm/mangossh/MainActivity.kt": [
        "NavigationRail",
        "Scaffold",
        "LazyColumn"
    ],
    "app/src/main/java/io/github/josxtm/mangossh/MangoViewModel.kt": ["MangoViewModel"],
    "app/src/main/java/io/github/josxtm/mangossh/ui/TerminalView.kt": ["TerminalView"],
    "app/src/main/res/drawable-nodpi/mangossh_launcher.jpg": []
}


def check(root, required=REQUIRED):
    root = Path(root)
    errors = []
    for relative, markers in required.items():
        target = root / relative
        if not target.is_file():
            errors.append(relative + ": approved GUI component not present")
            continue
        if markers:
            try:
                source = target.read_text(encoding="utf-8")
            except UnicodeError:
                errors.append(relative + ": unreadable GUI component")
                continue
            for marker in markers:
                if marker not in source:
                    errors.append(relative + ": expected UI landmark absent")
    return errors



def check_launcher(root):
    manifest = Path(root) / "app/src/main/AndroidManifest.xml"
    if not manifest.is_file():
        return ["missing Android launcher manifest"]
    try:
        document = ET.parse(manifest).getroot()
    except (OSError, ET.ParseError):
        return ["Android manifest cannot be parsed"]
    android = "{http://schemas.android.com/apk/res/android}"
    application = document.find("application")
    if application is None:
        return ["Android application element missing"]
    activities = application.findall("activity")
    compose = "io.github.josxtm.mangossh.MainActivity"
    legacy = ".MainActivity"
    problems = []
    active_compose = [a for a in activities if a.get(android + "name") == compose]
    active_legacy = [a for a in activities if a.get(android + "name") == legacy]
    if len(active_compose) != 1 or active_compose[0].get(android + "exported") != "true":
        problems.append("original Compose launcher Activity missing or disabled")
    if len(active_legacy) != 1 or active_legacy[0].get(android + "exported") != "false":
        problems.append("legacy Java Activity must remain internal and unexported")
    launchers = []
    for activity in activities:
        for route in activity.findall("intent-filter"):
            actions = {x.get(android + "name") for x in route.findall("action")}
            categories = {x.get(android + "name") for x in route.findall("category")}
            if "android.intent.action.MAIN" in actions and "android.intent.category.LAUNCHER" in categories:
                launchers.append(activity.get(android + "name"))
    if launchers != [compose]:
        problems.append("APK launcher must open only the original Compose GUI")
    return problems

if __name__ == "__main__":
    root = sys.argv[1] if len(sys.argv) > 1 else "."
    problems = check(root) + check_launcher(root)
    for problem in problems:
        print("GUI PARITY BLOCK:", problem)
    if problems:
        print("NOT APPROVED: restore original GUI and verify it visually.")
        sys.exit(1)
    print("Structural landmarks found; manual visual and functional approval still required.")
