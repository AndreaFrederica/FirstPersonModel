"""Reject wrong tags/versions before building or publishing a release."""
import os
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
properties = dict(
    line.split('=', 1) for line in (root / 'gradle.properties').read_text().splitlines()
    if '=' in line and not line.lstrip().startswith('#')
)
version = properties['mod_version'].strip()
if not re.fullmatch(r'[0-9]+\.[0-9]+\.[0-9]+[A-Za-z0-9.+-]*', version):
    raise SystemExit('Invalid mod_version')
tag = os.environ['RELEASE_TAG']
if tag != 'v' + version:
    raise SystemExit(f'Tag {tag!r} must match v{version}')
channel = os.environ.get('RELEASE_TYPE', 'release')
if channel not in ('release', 'beta', 'alpha'):
    raise SystemExit('Invalid release channel')

def revision(ref):
    return subprocess.check_output(['git', 'rev-parse', '--verify', ref], cwd=root, text=True).strip()

if revision(f'refs/tags/{tag}^{{commit}}') != revision('HEAD'):
    raise SystemExit('Checkout does not match the release tag')
source = (root / 'src/main/java/dev/tr7zw/firstperson/FirstPersonModelMod.java').read_text()
if f'version = "{version}"' not in source:
    raise SystemExit('Update the @Mod version in FirstPersonModelMod.java to match mod_version')
changelog = (root / 'CHANGELOG.md').read_text(encoding='utf-8')
if f'## {version}' not in changelog:
    raise SystemExit('Add this version to CHANGELOG.md')
values = {
    'version': version, 'tag': tag, 'release_type': channel,
    'jar': f'firstperson-1.12.2-{version}.jar',
    'sources': f'firstperson-1.12.2-{version}-sources.jar',
}
with open(os.environ['GITHUB_OUTPUT'], 'a', encoding='utf-8') as output:
    for key, value in values.items():
        output.write(f'{key}={value}\n')
print(f'Validated {tag} ({channel})')
