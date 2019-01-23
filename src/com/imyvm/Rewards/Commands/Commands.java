package com.imyvm.Rewards.Commands;

import com.imyvm.Rewards.Reward;
import com.imyvm.Rewards.Tools.SelfExpiringHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.util.*;

public class Commands implements CommandExecutor {
    private Reward plugin;

    public Commands(Reward pl) {
        plugin = pl;
    }

    private static List<String> rewards = Reward.getRewards();
    private static int time = Reward.getTime();
    private static int range = Reward.getRange();
    private static int mini = Reward.getMini();
    private SelfExpiringHashMap<String, String> map = new SelfExpiringHashMap<>(time);

    @Override
    public boolean onCommand(CommandSender sender, Command cmdObj, String label, String[] args) {
        if (args.length<1){
            return false;
        }
        String cmd = args[0];
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

        if (cmd.equalsIgnoreCase("give")){
            if (!sender.hasPermission("rewards.give")){
                return false;
            }
            if (args.length<3){
                sender.sendMessage("§e/Reward give [Player] [Which]");
                return false;
            }
            Player player1 = Bukkit.getPlayerExact(args[1]);
            if (player1 == null){
                sender.sendMessage("§4该玩家不存在或不在线！");
                return false;
            }
            List<String> rewards_name = getAllRewardsName(rewards);
            if (rewards_name.isEmpty()){
                sender.sendMessage("§4当前无任何奖励存在！");
                return false;
            }
            if (!rewards_name.contains(args[2])){
                sender.sendMessage("§4此奖励不存在！");
                return false;
            }
            long timestamp = new Date().getTime();
            String code = getRandomString();
            player1.sendMessage("§b请输入指令§f/rw ac "+code+" §b获取当前奖励§f[§e"+args[2]+"§f], §b有效期: §f"+time/1000 +"§b 秒");

            scheduler.runTaskTimerAsynchronously(plugin, () -> {
                long timestamp_now = new Date().getTime();
                long count = (time-timestamp_now+timestamp)/1000;
                player1.sendMessage("§b请输入指令§f/rw ac "+code+" §b获取当前奖励§f[§e"+args[2]+"§f], §b剩余时间: §f"+count +"§b 秒");
                if (count<=0){
                    scheduler.cancelTasks(plugin);
                }
            }, time/250+60, time/250+60);

            String value = args[2]+":"+code;
            String key = player1.getUniqueId().toString()+":"+code;
            map.put(key,value);
        }
        if (cmd.equalsIgnoreCase("add")){

            if (sender.hasPermission("rewards.add")){

                String info = String.join(":", args).replace("#", " "); //name:reward1:reward2:...
                rewards.add(info.substring(4));
                plugin.getConfig().set("Rewards", rewards);
                plugin.saveConfig();
                sender.sendMessage("§bRewards Added!");
            }else {
                sender.sendMessage("§4You don't have this permission!");
                return false;
            }
        }
        if (cmd.equalsIgnoreCase("acquire") || cmd.equalsIgnoreCase("ac")){
            if (args.length<2){
                sender.sendMessage("§f/rw ac [code]      -§e领取奖励");
                return false;
            }
            Player player = (Player) sender;
            String c = args[1].toUpperCase();
            if (!map.containsKey(player.getUniqueId().toString()+":"+c)){
                sender.sendMessage("§4当前没有奖励存在或代码错误！");
                return false;
            }else {
                String reward = map.get(player.getUniqueId().toString()+":"+c);
                List<String> it = new ArrayList<>(Arrays.asList(reward.split(":")));
                String code_id = it.get(1);
                if (!args[1].equalsIgnoreCase(code_id)){
                    sender.sendMessage("§4请输入正确的奖励代码！");
                    return false;
                }
                boolean success = runRewards(it.get(0), player);
                map.remove(player.getUniqueId().toString());
                scheduler.cancelTasks(plugin);
                return success;
            }
        }


        return true;
    }

    private List<String> getAllRewardsName(List<String> rewards){
        List<String> rewards_name = new ArrayList<>();
        if (rewards.isEmpty()){
            return rewards_name;
        }
        for (String s: rewards){
            List<String> it = new ArrayList<>(Arrays.asList(s.split(":")));
            rewards_name.add(it.get(0));
        }
        return rewards_name;
    }

    private List<String> getRewardCommands(String reward, List<String> rewards){
        List<String> commands = new ArrayList<>();
        if (rewards.isEmpty()){
            return commands;
        }
        for (String s: rewards){
            List<String> it = new ArrayList<>(Arrays.asList(s.split(":")));
            if (it.get(0).equalsIgnoreCase(reward)){
                commands = it.subList(1, it.size());
                break;
            }
        }
        return commands;
    }

    private boolean runRewards(String reward, Player player){
        List<String> commands = getRewardCommands(reward, rewards);
        double d = Math.random();
        int random = (int)(d*range+mini);
        if (commands.isEmpty()){
            return false;
        }
        for (String ss:commands){
            if (ss.contains("{player}") || ss.contains("{random}")){
                String ss1 = ss.replace("{player}", player.getName());
                String ss2 = ss1.replace("{random}", String.valueOf(random));
                String strStart = "<";
                String strEnd = ">";
                if (ss2.contains(strStart) && ss2.contains(strEnd)){
                    String ex = subString(ss2, strStart, strEnd);
                    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("nashorn");
                    try {
                        String result = String.valueOf(scriptEngine.eval(ex));
                        ss2 = ss2.replace("<"+ex+">", result);
                    }catch (ScriptException e){
                        e.printStackTrace();
                    }
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ss2);
            }else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ss);
            }
        }
        return true;
    }

    private static String subString(String str, String strStart, String strEnd) {

        /* 找出指定的2个字符在 该字符串里面的 位置 */
        int strStartIndex = str.indexOf(strStart);
        int strEndIndex = str.indexOf(strEnd);

        /* index 为负数 即表示该字符串中 没有该字符 */
        if (strStartIndex < 0) {
            return "";
        }
        if (strEndIndex < 0) {
            return "";
        }

        return str.substring(strStartIndex, strEndIndex).substring(strStart.length());
    }

    private static synchronized String getRandomString(){
        String range = "0123456789abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuffer result;
        result = new StringBuffer();
        for ( int i = 0; i < 4; i++ ){
            result.append( range.charAt( random.nextInt( range.length() ) ) );
        }
        return result.toString().toUpperCase();
    }


}
