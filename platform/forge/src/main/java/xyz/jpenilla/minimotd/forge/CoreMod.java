package xyz.jpenilla.minimotd.forge;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;
import xyz.jpenilla.minimotd.common.Constants;

import java.util.Map;

@IFMLLoadingPlugin.Name(Constants.PluginMetadata.NAME)
public class CoreMod implements IFMLLoadingPlugin {
  @Override
  public String[] getASMTransformerClass() {
    return new String[0];
  }

  @Override
  public String getModContainerClass() {
    return null;
  }

  @Nullable
  @Override
  public String getSetupClass() {
    return null;
  }

  @Override
  public void injectData(Map<String, Object> data) {

  }

  @Override
  public String getAccessTransformerClass() {
    return null;
  }
}
