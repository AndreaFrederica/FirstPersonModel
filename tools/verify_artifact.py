"""Validate the installable JAR's metadata and production Mixin refmap."""
import json
from pathlib import Path
from zipfile import ZipFile

root = Path(__file__).resolve().parents[1]
jars = [p for p in (root / 'build/libs').glob('*.jar') if not p.stem.endswith(('-dev', '-sources'))]
assert len(jars) == 1, jars
with ZipFile(jars[0]) as jar:
    metadata = json.loads(jar.read('mcmod.info'))[0]
    assert metadata['modid'] == 'firstperson'
    assert metadata['mcversion'] == '1.12.2'
    assert '${' not in metadata['version']
    manifest = jar.read('META-INF/MANIFEST.MF').decode()
    assert 'FMLCorePlugin: dev.tr7zw.firstperson.mixin.FirstPersonMixinLoader' in manifest
    assert 'FMLCorePluginContainsFMLMod: true' in manifest
    config = json.loads(jar.read('firstperson.mixins.json'))
    refmap = json.loads(jar.read(config['refmap']))
    mappings = refmap['mappings']
    for mixin in config['client']:
        class_name = config['package'].replace('.', '/') + '/' + mixin
        assert class_name + '.class' in jar.namelist(), class_name
        assert mappings.get(class_name), 'No production mappings: ' + class_name
    assert 'func_180446_a' in json.dumps(refmap), 'renderEntities is not remapped'
    assert 'func_187457_a' in json.dumps(refmap), 'first-person item renderer is not remapped'
    # WrapMethod selectors need refmap entries even when the remapper does not
    # recognize the MixinExtras annotation itself.
    model = mappings['dev/tr7zw/firstperson/mixin/ModelRendererMixin']
    assert 'func_78785_a' in model.get('render', ''), 'ModelRenderer.render is not remapped'
    assert 'func_78791_b' in model.get('renderWithRotation', ''), 'ModelRenderer.renderWithRotation is not remapped'
    fishing = mappings['dev/tr7zw/firstperson/mixin/FishingRendererMixin']
    assert 'func_76986_a' in fishing.get('doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V', ''), 'RenderFish.doRender is not remapped'
    for locale in ('en_us', 'zh_cn'):
        language = jar.read(f'assets/firstperson/lang/{locale}.lang').decode('utf-8')
        assert 'key.firstperson.toggle=' in language
    assert 'LICENSE' in jar.namelist()
print('Verified:', jars[0])
