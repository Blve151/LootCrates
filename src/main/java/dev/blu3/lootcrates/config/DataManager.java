package dev.blu3.lootcrates.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.blu3.lootcrates.config.adapters.OptionalTypeAdapter;
import dev.blu3.lootcrates.config.types.CrateData;
import dev.blu3.lootcrates.config.types.GeneralConfig;
import dev.blu3.lootcrates.utils.CrateManager;
import dev.blu3.lootcrates.utils.objects.CrateTier;
import dev.blu3.lootcrates.utils.objects.LootCrate;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static dev.blu3.lootcrates.utils.Utils.regex;


public class DataManager {
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY).enableComplexMapKeySerialization().create();
    private static final File mainDir = new File("./config/lootcrates");
    private static final File configDir = new File(mainDir, "/config");
    private static final File dataDir = new File(mainDir, "/data");

    private final File generalFile = new File(configDir, "config.json");
    private final File crateDataFile = new File(dataDir, "crate_data.json");

    private GeneralConfig general = new GeneralConfig();
    private CrateData crateData = new CrateData();

    private final CrateManager crateManager = new CrateManager();
    public void load(Optional<ServerCommandSource> optSender) {
        try{
            if(!mainDir.exists()){
                mainDir.mkdirs();
            }
            if(!configDir.exists()){
                configDir.mkdirs();
            }
            if(!dataDir.exists()){
                dataDir.mkdirs();
            }
            general = (GeneralConfig) handleFile(generalFile, general);
            crateData = (CrateData) handleFile(crateDataFile, crateData);
            optSender.ifPresent(sender -> sender.sendMessage(Text.literal("§a[!] Reloaded LootCrate configs.")));
        }catch (Exception ex){
            exceptionThrown(ex, optSender);
        }
    }

    public void exceptionThrown (Exception ex, Optional<ServerCommandSource> optSender){
        optSender.ifPresent(sender -> sender.sendError(Text.literal("[!] An error occurred while loading LootCrate configs, check console for errors.")));
        ex.printStackTrace();
    }

    public Object handleFile(File file, Object obj) throws IOException{
        if(!file.exists()){
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(obj));
            fw.close();
        }else{
            FileReader fr = new FileReader(file);
            obj = gson.fromJson(fr, TypeToken.of(obj.getClass()).getType());
            fr.close();
        }
        return obj;
    }
    private void updateCrateDataFile() {
        CompletableFuture.runAsync(() -> {
            try {
                FileWriter fw = new FileWriter(crateDataFile);
                fw.write(gson.toJson(crateData));
                fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public Text getMsg (String node){
        return Text.literal(regex(this.general.messages.get((node))));
    }

    public String getMsgStr (String node){
        return regex(this.general.messages.get(node));
    }
    private static final Random random = new Random();

    public CrateTier getRandomTier() {
        List<CrateTier> crateTiers = new ArrayList<>();
        for (CrateTier crateTier : this.general.crateTiers) {
            for (int i = 0; i < crateTier.chance; i++) {
                crateTiers.add(crateTier);
            }
        }
        return crateTiers.get(random.nextInt(crateTiers.size()));
    }

    public GeneralConfig getGeneral() {
        return general;
    }

    public final Optional<LootCrate> getCurrentCrate() {
        return this.crateData.currentCrate;
    }

    public void setCurrentCrate (LootCrate crate) {
        this.crateData.currentCrate = Optional.of(crate);
        updateCrateDataFile();
    }

    public void setCurrentCrateEmpty () {
        this.crateData.currentCrate = Optional.empty();
        updateCrateDataFile();
    }

    public Long getNextCrateDrop () {
        return this.crateData.nextCrateDrop;
    }

    public void setNextCrateDrop (long timeMs) {
        this.crateData.nextCrateDrop = timeMs;
        updateCrateDataFile();
    }

    public Long getLastCrateDrop () {
        return this.crateData.lastCrateDrop;
    }

    public void setLastCrateDrop (long timeMs) {
        this.crateData.lastCrateDrop = timeMs;
        updateCrateDataFile();
    }

    public String getLastClaimedPlayer () {
        return this.crateData.lastCrateClaimed;
    }
    public void setLastClaimedPlayer (String name) {
        this.crateData.lastCrateClaimed = name;
        updateCrateDataFile();
    }

    public String getLastTierName (){
        return this.crateData.lastTierName;
    }

    public void setLastTierName (String tier){
        this.crateData.lastTierName = tier;
        updateCrateDataFile();
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }
}
