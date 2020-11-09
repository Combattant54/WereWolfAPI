package io.github.ph1lou.werewolfapi.rolesattributs;

import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.WereWolfAPI;
import io.github.ph1lou.werewolfapi.enumlg.*;
import io.github.ph1lou.werewolfapi.events.*;
import io.github.ph1lou.werewolfapi.versions.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class RolesImpl implements Roles, Listener,Cloneable {
    
    public final WereWolfAPI game;
    public final GetWereWolfAPI main;

    @NotNull
    private UUID uuid;
    private Boolean infected = false;
    @NotNull
    private final String key;

    public RolesImpl(GetWereWolfAPI main, WereWolfAPI game, @NotNull UUID uuid,@NotNull String key){
        this.game=game;
        this.main = main;
        this.uuid=uuid;
        this.key=key;
    }


    @Override
    public @NotNull String getKey() {
        return key;
    }


    @Override
    public void recoverPower() {

        Player player = Bukkit.getPlayer(uuid);

        if (player==null) return;

        PlayerWW plg = game.getPlayersWW().get(uuid);
        plg.setKit(true);
        Sounds.EXPLODE.play(player);
        player.sendMessage(getDescription());
        player.sendMessage(game.translate("werewolf.announcement.review_role"));

        recoverPotionEffect();

        if(!game.getStuffs().getStuffRoles().containsKey(getKey())){
            Bukkit.getConsoleSender().sendMessage("[WereWolfPlugin] invalid addon structure");
            return;
        }

        for(ItemStack i:game.getStuffs().getStuffRoles().get(plg.getRole().getKey())) {

            if(player.getInventory().firstEmpty()==-1) {
                player.getWorld().dropItem(player.getLocation(),i);
            }
            else {
                player.getInventory().addItem(i);
                player.updateInventory();
            }
        }

    }


    @NotNull
    @Override
    public UUID getPlayerUUID(){
        return uuid;
    }

    @Override
    public void setPlayerUUID(@NotNull UUID uuid) {
        this.uuid=uuid;
    }

    @Override
    public void recoverPowerAfterStolen() {
    }

    @Override
    public boolean isKey(@NotNull String s) {
        return s.equals(this.getKey());
    }

    @Override
    public @NotNull Camp getCamp() {

        if(isNeutral()){
            return Camp.NEUTRAL;
        }

        if(isWereWolf()){
            return Camp.WEREWOLF;
        }
        return Camp.VILLAGER;
    }

    @Override
    public boolean isCamp(@NotNull Camp camp) {
        return camp.equals(getCamp());
    }

    @Override
    public Roles publicClone() {
        try {
            return (Roles) clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRecoverHeart(DayEvent event) {

        PlayerWW playerWW = game.getPlayerWW(getPlayerUUID());

        if(playerWW==null) return;

        if (!playerWW.isState(StatePlayer.ALIVE)) return;

        Player player = Bukkit.getPlayer(getPlayerUUID());

        if (player == null) return;

        if (playerWW.getLostHeart() > 0) {
            VersionUtils.getVersionUtils().setPlayerMaxHealth(player, VersionUtils.getVersionUtils().getPlayerMaxHealth(player) + playerWW.getLostHeart());
            playerWW.clearLostHeart();
        }

    }

    @EventHandler
    public void onModeratorScoreBoard(UpdateModeratorNameTag event){

        PlayerWW playerWW = game.getPlayerWW(getPlayerUUID());
        StringBuilder sb = new StringBuilder(event.getSuffix());
        StringBuilder sb2 = new StringBuilder(event.getPrefix());
        if(!event.getPlayerUUID().equals(getPlayerUUID())) return;

        if(playerWW==null) return;

        if(playerWW.isState(StatePlayer.DEATH)){
            return;
        }


        if(!playerWW.getLovers().isEmpty()){
            sb.append(ChatColor.LIGHT_PURPLE).append("L ");
        }

        if(playerWW.getAmnesiacLoverUUID()!=null){
            sb.append(ChatColor.DARK_PURPLE).append("AL ");
        }

        if(playerWW.getCursedLovers()!=null){
            sb.append(ChatColor.BLACK).append("CL ");
        }

        if(isNeutral()){
            sb2.append(ChatColor.GOLD);
        }
        else if(isWereWolf()){
            sb2.append(ChatColor.DARK_RED);
        }
        else sb2.append(ChatColor.GREEN);

        event.setPrefix(sb2.toString());
        event.setSuffix(sb.toString());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRequestWereWolfList(RequestSeeWereWolfListEvent event){

        if(!event.getPlayerUUID().equals(getPlayerUUID())) return;
        if(game.getPlayersWW().get(getPlayerUUID()).isState(StatePlayer.DEATH)) return;

        if (game.getConfig().getTimerValues().get("werewolf.menu.timers.werewolf_list") <= 0) {
            event.setAccept(isWereWolf());
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAppearInWereWolfList(AppearInWereWolfListEvent event){

        if(!event.getPlayerUUID().equals(getPlayerUUID())) return;
        if(game.getPlayersWW().get(getPlayerUUID()).isState(StatePlayer.DEATH)) return;
        event.setAppear(isWereWolf());
    }

    @EventHandler
    public void onWereWolfList(WereWolfListEvent event){

        PlayerWW lg = game.getPlayersWW().get(uuid);

        if(!isWereWolf()) return;

        if(!lg.isState(StatePlayer.ALIVE)) return;

        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;

        player.sendMessage(game.translate("werewolf.role.werewolf.see_others"));
        Sounds.WOLF_HOWL.play(player);
    }

    @EventHandler
    public void onNewWereWolf(NewWereWolfEvent event) {

        if(!uuid.equals(event.getUuid())) return;

        Player player = Bukkit.getPlayer(uuid);

        if(player!=null) {
            player.sendMessage(game.translate("werewolf.role.werewolf.go_to_the_werewolf_camp"));
            Sounds.WOLF_HOWL.play(player);
            recoverPotionEffect();
        }

        game.getPlayersWW().values().stream()
                .filter(playerWW -> playerWW.getRole().isWereWolf())
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                .map(playerWW -> Bukkit.getPlayer(playerWW.getRole().getPlayerUUID()))
                .filter(Objects::nonNull)
                .forEach(player1 -> {
                    player1.sendMessage(game.translate("werewolf.role.werewolf.new_werewolf"));
                    Sounds.WOLF_HOWL.play(player1);
                });


    }

    @EventHandler
    private void onPlayerDeathForInfected(PlayerDeathEvent event) {

        if(!infected) return;

        if(event.getEntity().getKiller()==null) return;
        Player killer = event.getEntity().getKiller();

        if(!killer.getUniqueId().equals(getPlayerUUID())) return;

        killer.removePotionEffect(PotionEffectType.ABSORPTION);
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1200, 0, false, false));
        killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 0, false, false));
    }

    @Override
    public boolean isWereWolf() {
        return infected;
    }

    @Override
    public boolean getInfected() {
        return infected;
    }

    @Override
    public void setInfected() {
        this.infected = true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInfectedDay(DayEvent event) {

        if (!getInfected()) return;

        if (!game.getPlayersWW().get(getPlayerUUID()).isState(StatePlayer.ALIVE)) return;

        Player player = Bukkit.getPlayer(getPlayerUUID());

        if(player==null) return;

        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInfectedNight(NightEvent event) {

        if (!getInfected()) return;

        if (!game.getPlayersWW().get(getPlayerUUID()).isState(StatePlayer.ALIVE)) return;

        Player player = Bukkit.getPlayer(getPlayerUUID());

        if(player==null) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, -1, false, false));
    }

    @EventHandler
    public void onActionBarGameLoverEvent(ActionBarEvent event){

        if(!game.isState(StateGame.GAME)) return;

        if(!event.getPlayerUUID().equals(getPlayerUUID())) return;

        StringBuilder sb = new StringBuilder(event.getActionBar());
        PlayerWW plg = game.getPlayerWW(event.getPlayerUUID());
        Player player = Bukkit.getPlayer(getPlayerUUID());

        if(player==null) return;

        assert plg != null;

        if(!plg.isState(StatePlayer.ALIVE)) return;

        buildActionbarLover(player,sb,plg.getLovers());


        if(plg.getAmnesiacLoverUUID()!=null && plg.getRevealAmnesiacLover()) {
            buildActionbarLover(player,
                    sb,
                    Collections.singletonList(plg.getAmnesiacLoverUUID()));
        }

        event.setActionBar(sb.toString());

    }

    public void buildActionbarLover(Player player,StringBuilder sb, List<UUID> list){

        list
                .stream()
                .map(game::getPlayerWW)
                .filter(Objects::nonNull)
                .filter(playerWW -> playerWW.isState(StatePlayer.ALIVE))
                .peek(playerWW -> sb.append("§d ")
                        .append(playerWW.getName())
                        .append(" "))
                .map(PlayerWW::getRole)
                .map(Roles::getPlayerUUID)
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player1 -> sb
                        .append(game.getScore()
                                .updateArrow(player,
                                        player1.getLocation())));
    }

    @EventHandler
    public void onEndPlayerMessageInfected(EndPlayerMessageEvent event){

        if(!infected) return;

        if(!event.getPlayerUUID().equals(getPlayerUUID())) return;

        StringBuilder sb = event.getEndMessage();

        sb.append(game.translate("werewolf.end.infect"));
    }

    @Override
    public void recoverPotionEffect() {

        if(!infected) return;

        Player player = Bukkit.getPlayer(getPlayerUUID());

        if(player==null) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,Integer.MAX_VALUE,0,false,false));
        if(game.isDay(Day.DAY)) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,-1,false,false));
    }



    @EventHandler
    public void onCountCategories(CountRemainingRolesCategoriesEvent event){

        if(isNeutral()){
            event.addNeutral();
            return;
        }

        if(isWereWolf()){
            event.addWerewolf();
            return;
        }

        event.addVillager();
    }
}