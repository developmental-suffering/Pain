package pojlib.account;

import android.app.Activity;
import com.google.gson.Gson;
import org.json.JSONException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.annotation.Nullable;
import pojlib.util.Constants;
import pojlib.util.GsonUtils;
import pojlib.util.Logger;
import pojlib.util.MSAException;

public class MinecraftAccount {
    public String accessToken;
    public String uuid;
    public String username;
    public boolean isDemoMode = false;
    public long expiresOn;
    public final String userType = "msa";

    public static MinecraftAccount login(Activity activity, String gameDir, String msToken) throws MSAException, IOException, JSONException {
        MinecraftAccount account = new MinecraftAccount();
        
        // 1. Hardcode structural properties to satisfy Mojang client arguments
        account.username = "Player"; // Choose any default single-player name
        account.uuid = "00000000-0000-0000-0000-000000000000"; // Valid blank UUID format
        account.accessToken = "00000000000000000000000000000000"; // Dummy local session token
        account.isDemoMode = false; // Bypasses Mojang's internal 90-minute trial flag
        account.expiresOn = System.currentTimeMillis() + 31536000000L; // Force token active for 1 year
        
        // 2. Write the structural JSON locally so the engine's 'load' mechanism doesn't fail
        GsonUtils.objectToJsonFile(gameDir + "/" + account.uuid + ".json", account);
        
        return account;
    }

    public static boolean removeAccount(Activity activity, String uuid) {
        File accountFile = new File(activity.getFilesDir() + "/accounts/" + uuid + ".json");
        File accountCache = new File(Constants.USER_HOME + "/cache_data");

        return accountFile.delete() && accountCache.delete();
    }

    // Try this before using login - modified to fallback safely to a dummy account if file is missing
    public static MinecraftAccount load(String path, String uuid) {
        File sessionFile = new File(path + "/" + uuid + ".json");
        
        if (sessionFile.exists()) {
            MinecraftAccount loadedAccount = GsonUtils.jsonFileToObject(sessionFile.getAbsolutePath(), MinecraftAccount.class);
            if (loadedAccount != null) {
                return loadedAccount;
            }
        }
        
        MinecraftAccount dummy = new MinecraftAccount();
        dummy.username = "Player";
        dummy.uuid = "00000000-0000-0000-0000-000000000000";
        dummy.accessToken = "00000000000000000000000000000000";
        dummy.isDemoMode = false;
        dummy.expiresOn = System.currentTimeMillis() + 31536000000L;
        
        return dummy;
    }

    public static String getSkinFaceUrl(MinecraftAccount account) {
        if (account.isDemoMode) {
            return Constants.MINOTAR_URL + "/helm/MHF_Steve";
        } else {
            try {
                return Constants.MINOTAR_URL + "/helm/" + account.uuid;
            } catch (NullPointerException e) {
                Logger.getInstance().appendToLog("Username likely not set! Please set your username at Minecraft.net and try again. | " + e);
                return null;
            }
        }
    }
}
