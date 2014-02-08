package com.worldcretornica.emoteme;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class EmoteMeListener implements Listener
{
    private EmoteMe plugin;
    
    public EmoteMeListener(EmoteMe instance) {
        plugin = instance;
    }

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) 
	{
		String message = event.getMessage();
		Player p = event.getPlayer();
		
        if(message.startsWith("/"))
		{
        	message = message.substring(1);
        	if(CheckEmote(message, p))
        		event.setCancelled(true);
        }
    }
	
	@EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(ServerCommandEvent event) 
	{
		String message = event.getCommand();

        CheckEmote(message, null);
    }
	
	private boolean CheckEmote(String message, Player p)
	{
		String[] args = message.split(" ");
		
		String name = "";
		
		if(p == null)
		{
			name = "Server";
		}
		else
		{
			name = p.getDisplayName();
		}
		
    	if(args != null && args.length > 0)
    	{
    		if(EmoteMe.emotelist.containsKey(args[0]))
    		{
    			EmoteMeCommand emc = EmoteMe.emotelist.get(args[0]);
    			
    			if(p == null || p.hasPermission("emoteme." + emc.name) || p.hasPermission("emoteme.all"))
    			{
    				String selftext = "";
    				String othertext = "";
    				String recipienttext = "";
    				
    				if(args.length > 1)
    				{
    					Player recipient = Bukkit.getPlayer(args[1]);
    					if(recipient != null)
    					{
    						args[1] = recipient.getDisplayName();
    					}
    					else
    					{
    						args[1] = args[1].replace("&", "");
    					}
    					
    					selftext = emc.sendertext2.replace("%sender", name);
        				othertext = emc.othertext2.replace("%sender", name);
        				recipienttext = emc.recipienttext.replace("%sender", name);
    				} 
    				else
    				{
    					selftext = emc.sendertext.replace("%sender", name);
        				othertext = emc.othertext.replace("%sender", name);
        				recipienttext = emc.recipienttext.replace("%sender", name);
    				}
    				
    				for(int ctr = 1; ctr < args.length; ctr++)
    				{
    					if(ctr > 1)
    					{
    						args[ctr] = args[ctr].replace("&", "");
    					}
    					
    					selftext = selftext.replace("%" + ctr, args[ctr]);
    					othertext = othertext.replace("%" + ctr, args[ctr]);
    					recipienttext = recipienttext.replace("%" + ctr, args[ctr]);
    				}
    				
    				//selftext = ChatColor.translateAlternateColorCodes('&', selftext);
    				//othertext = ChatColor.translateAlternateColorCodes('&', othertext);
    				//recipienttext = ChatColor.translateAlternateColorCodes('&', recipienttext);
    				
    				if(p == null)
    				{
    				    plugin.getLogger().info(ChatColor.stripColor(selftext));
    				}
    				else
    				{
    					plugin.getLogger().info(ChatColor.stripColor(othertext));
    				}
    				
    				/*for(Player pl : Bukkit.getOnlinePlayers())
    				{
    					if(p != null && pl.equals(p))
    					{
    						pl.sendMessage(selftext);
    					}
    					else if(args.length > 1 && pl.getDisplayName().equalsIgnoreCase(args[1]))
    					{
    						pl.sendMessage(recipienttext);
    					}
    					else
    					{
    						pl.sendMessage(othertext);
    					}
    				}*/
    				
    				if(args.length > 1) {
    				    plugin.broadcastOthers(othertext, p.getName(), args[1]);
    				    plugin.broadcastRecipient(recipienttext, args[1]);
    				} else {
    				    plugin.broadcastOthers(othertext, p.getName(), "");
    				}
    				plugin.broadcastSender(selftext, p.getName());
    				
    				return true;
    			}
    		}
    	}
    	return false;
	}
}
