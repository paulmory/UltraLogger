package org.ultralogger.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.ultralogger.Main;

public class HistoryManager implements Listener{
	
	public static File history = new File("./Log/block_historic");
	public static HistoryManager instance;
	
	private Main plugin;
	
	private ArrayList<History> historic = new ArrayList<History>();
	private int itemID = 280;
	private int count =0;
	
	public HistoryManager(Main mainLogger,int id) {
		instance=this;
		plugin=mainLogger;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.itemID=id;
		if(!history.exists()){
			try {
				history.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e){
		String time = DateFormat.getInstance().format(new Date(System.currentTimeMillis()))+" ";
		Block i = e.getBlock();
		String msg ="-"+time+printPlayer(e.getPlayer())+" broke "+printBlock(i);
		int index =getHistoricIndex(i.getLocation());
		if(index==-1){
			History h =new History(i.getLocation());
			h.add(msg);
			historic.add(h);
		}
		else
			historic.get(index).add(msg);
		count++;
		if(count%100==0){
			try {
				save();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	private String printBlock(Block i) {
		return "{"+i.getTypeId()+"}";
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e){
		String time = DateFormat.getInstance().format(new Date(System.currentTimeMillis()))+" ";
		Block i = e.getBlock();
		String msg ="-"+time+printPlayer(e.getPlayer())+" placed "+printBlock(i);
		int index =getHistoricIndex(i.getLocation());
		if(index==-1){
			History h = new History(i.getLocation());
			h.add(msg);
			historic.add(h);
		}
		else
			historic.get(index).add(msg);
		count++;
		if(count%100==0){
			count=0;
			try {
				save();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		Player p =e.getPlayer();
		if(e.getAction()!=Action.RIGHT_CLICK_BLOCK || !e.hasItem() || !Main.canSeeHistory(p))
			return;
		int id = e.getPlayer().getItemInHand().getTypeId();
		if(id!=itemID)
			return ;
		Location loc = e.getClickedBlock().getLocation();
		int index = getHistoricIndex(loc);
		if(index==-1)
			p.sendMessage(ChatColor.RED+"No data found for this location !");
		else
			p.sendMessage(historic.get(index).whatHappened());		
	}
	
	@SuppressWarnings("unchecked")
	public int getHistoricIndex(Location loc){
		int x =0;
		for(Iterator<History> i = ((ArrayList<History>) historic.clone()).iterator();i.hasNext();){
			History g = i.next();
			if(equal(g.getLocation(),loc))
				return x;
			x++;
		}
		return -1;
	}
	
	public History getHistory(Location loc){
		if(getHistoricIndex(loc)<=-1)
			return null;
		return historic.get(getHistoricIndex(loc));
	}

	public void save() throws Exception{
		PrintWriter out = new PrintWriter(history);
		for(@SuppressWarnings("unchecked")
		Iterator<History> i = ((ArrayList<History>) historic.clone()).iterator();i.hasNext();){
			out.println(i.next().toString());
		}
		out.close();
	}
	public void load() throws Exception{
		BufferedReader r = new BufferedReader(new FileReader(history));
		Server v = plugin.getServer();
		String s="";
		while((s=r.readLine())!=null){
			History h =History.fromString(s, v);
			if(h==null)
				continue;
			historic.add(h);
		}
		r.close();
	}
	
	/*
	 * STATIC USEFUL METHODS
	 */
	
	public static String printPlayer(Player player) {
		String name = player.getName();
		if(Main.isAdmin(player))
			name="[Admin] "+name;
		name="("+player.getGameMode().name()+")"+name;
		return name;
	}
	
	public static HistoryManager getInstance(){
		return instance;
	}
	
	/**Used to compare in an  HashMap
	 * 
	 * @param location
	 * @param loc
	 * @return if these two loc are equals but with their values
	 */
	public static boolean equal(Location location, Location loc) {
		int x = location.getBlockX(),y = location.getBlockY(),z = location.getBlockZ();
		World w = location.getWorld();
		if( w==loc.getWorld() && x==loc.getBlockX() && y==loc.getBlockY() && z==loc.getBlockZ() )
			return true;
		return false;
	}
}
