import java.util.List;
import java.util.Map;

import net.minecraftforge.gradle.GradleStartCommon;

public class GradleStartServer extends GradleStartCommon
{
    public static void main(String[] args) throws Throwable
    {
        (new GradleStartServer()).launch(args);
    }
    
    @Override
    protected String getTweakClass()
    {
        return "";
    }
    
    @Override
    protected String getBounceClass()
    {
       return "net.minecraft.server.MinecraftServer";
    }

    @Override protected void preLaunch(Map<String, String> argMap, List<String> extras) { }
    @Override protected void setDefaultArguments(Map<String, String> argMap) { }
}
