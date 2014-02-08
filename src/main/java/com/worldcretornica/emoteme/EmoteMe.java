package com.worldcretornica.emoteme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.event.EventListener;
import lilypad.client.connect.api.event.MessageEvent;
import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.api.result.FutureResultListener;
import lilypad.client.connect.api.result.StatusCode;
import lilypad.client.connect.api.result.impl.MessageResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class EmoteMe extends JavaPlugin {

	public static String NAME;
	public static String PREFIX;

	public static Map<String, EmoteMeCommand> emotelist = null;
	
	private final static String channelname = "wcEmoteMe";

	@Override
	public void onDisable() 
	{
		emotelist = null;
		PREFIX = null;
		NAME = null;
	}

	@Override
	public void onEnable() {

		SetupConfig();

		getServer().getPluginManager().registerEvents(new EmoteMeListener(this), this);
		
		getConnect().registerEvents(this);
	}
	
	private Connect getConnect() {
	    return super.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
    }
	
	
	public void broadcastSender(final String text, final String sender) {
        try {
            Connect connect = getConnect();

            connect.request(new MessageRequest("", channelname, "SENDER;" + text.replace(";", "") + ";" + sender.replace(";", ""))).registerListener(new FutureResultListener<MessageResult>() {
                public void onResult(MessageResult redirectResult) {
                    if (redirectResult.getStatusCode() == StatusCode.SUCCESS) {
                        return;
                    }
                }
            });

        } catch (Exception e) {
        }
    }
	
	public void broadcastRecipient(final String text, final String recipient) {
        try {
            Connect connect = getConnect();

            connect.request(new MessageRequest("", channelname, "RECIPIENT;" + text.replace(";", "") + ";" + recipient.replace(";", ""))).registerListener(new FutureResultListener<MessageResult>() {
                public void onResult(MessageResult redirectResult) {
                    if (redirectResult.getStatusCode() == StatusCode.SUCCESS) {
                        return;
                    }
                }
            });

        } catch (Exception e) {
        }
    }
	
	public void broadcastOthers(final String text, final String sender, final String recipient) {
        try {
            Connect connect = getConnect();

            connect.request(new MessageRequest("", channelname, "ALL;" + text.replace(";", "") + ";" + sender.replace(";", "") + ";" + recipient.replace(";", ""))).registerListener(new FutureResultListener<MessageResult>() {
                public void onResult(MessageResult redirectResult) {
                    if (redirectResult.getStatusCode() == StatusCode.SUCCESS) {
                        return;
                    }
                }
            });

        } catch (Exception e) {
        }
    }
	
	@EventListener
    public void onMessage(MessageEvent event) {
        if (event.getChannel().equals(channelname)) {
            try {
                String[] tokens;
                
                tokens = event.getMessageAsString().split(";");
                
                String action = tokens[0];
                String text;
                String sender;
                String recipient;
                
                switch (action) {
                case "SENDER":
                    if(tokens.length >= 3) {
                        text = tokens[1];
                        sender = tokens[2];
                        
                        Player p = Bukkit.getPlayerExact(sender);
                        
                        if(p != null) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
                        }
                    }
                    break;
                case "RECIPIENT":
                    if(tokens.length >= 3) {
                        text = tokens[1];
                        recipient = tokens[2];
                        
                        Player p = Bukkit.getPlayerExact(recipient);
                        
                        if(p != null) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
                        }
                    }
                    break;
                case "ALL":
                    if(tokens.length >= 3) {
                        text = tokens[1];
                        sender = tokens[2];
                        
                        if(tokens.length >= 4) {
                            recipient = tokens[3];
                        } else {
                            recipient = "";
                        }
                        
                        Player p1 = Bukkit.getPlayerExact(sender);
                        Player p2 = Bukkit.getPlayerExact(recipient);
                        
                        for(Player pl : Bukkit.getOnlinePlayers())
                        {
                            if((p1 == null || !pl.equals(p1)) && (p2 == null || !pl.equals(p2)))
                            {
                                pl.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
                            }
                        }
                    }
                    break;
                }

            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }

    }
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args)
	{
		if(label.equalsIgnoreCase("emoteme"))
		{
			if(args.length == 0)
			{
				sender.sendMessage("-=EmoteMe Command list=-");
				String commands = " ";
				List<String> commandlist = new ArrayList<String>();
				
				for(EmoteMeCommand emc : emotelist.values())
				{
					commandlist.add(emc.name);
				}
				
				Collections.sort(commandlist);
				
				for(String cmd : commandlist)
				{
					commands += cmd + " ";
				}
				
				sender.sendMessage(commands);
				return true;
			} 
			else 
			{
				String a0 = args[0];
				
				if(a0.equalsIgnoreCase("help"))
				{
					if(args.length == 1)
					{
						sender.sendMessage("Usage : /emoteme help <emote name>");
						sender.sendMessage("Example : /emoteme help " + emotelist.values().toArray()[0]);
						return true;
					}
					else
					{
						String name = args[1];
						
						if(emotelist.containsKey(name))
						{
							sender.sendMessage("Help for " + name);
							sender.sendMessage(" " + emotelist.get(name).help);
							return true;
						}
						else
						{
							sender.sendMessage("Unknown emote, type /emoteme for a list of emotes");
							return true;
						}
					}
				}
				else if(a0.equalsIgnoreCase("reload"))
				{
					if(sender.hasPermission("emoteme.admin"))
					{
						sender.sendMessage("Reloading EmoteMe configurations...");
						SetupConfig();
						sender.sendMessage("Config reloaded!");
						return true;
					}
				}
			}
		}
		return false;
	}

	public void SetupConfig() 
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		NAME = pdfFile.getName();
		PREFIX = ChatColor.BLUE + "[" + NAME + "] " + ChatColor.RESET;
		String configpath = getDataFolder().getAbsolutePath();

		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdirs();
		}

		File configfile = new File(configpath, "config.yml");

		if (!configfile.exists()) {
			createConfig(configfile);
		}

		FileConfiguration config = new YamlConfiguration();

		try {
			config.load(configfile);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			getLogger().severe(PREFIX + "can't read configuration file");
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
		    getLogger().severe(PREFIX + "invalid configuration format");
			e.printStackTrace();
		}

		Map<String, Object> commandlist = config.getValues(true);

		if (commandlist != null) {
			emotelist = new HashMap<String, EmoteMeCommand>();

			String currentcommand = "";
			String currentsender = "";
			String currentsender2 = "";
			String currentothers = "";
			String currentothers2 = "";
			String currentrecipient = "";
			String currenthelp = "";

			for (String key : commandlist.keySet()) {
				if (!key.contains(".")) {
					if (!currentcommand.equalsIgnoreCase("")) {
						EmoteMeCommand newcommand = new EmoteMeCommand();
						newcommand.name = currentcommand;
						newcommand.othertext = currentothers;
						newcommand.othertext2 = currentothers2;
						newcommand.recipienttext = currentrecipient;
						newcommand.sendertext = currentsender;
						newcommand.sendertext2 = currentsender2;
						newcommand.help = currenthelp;

						if (!emotelist.containsKey(currentcommand)) {
							emotelist.put(currentcommand, newcommand);
						} else {
						    getLogger().warning(PREFIX + " emote command '" + currentcommand + "' was already registered.");
						}
					}

					currentcommand = key;
					currentsender = "";
					currentsender2 = "";
					currentothers = "";
					currentothers2 = "";
					currentrecipient = "";
					currenthelp = "";
				} else {
					if (key.endsWith(".sender")) {
						currentsender = (String) commandlist.get(key);
					} else if (key.endsWith(".sender2")) {
						currentsender2 = (String) commandlist.get(key);
					} else if (key.endsWith(".others")) {
						currentothers = (String) commandlist.get(key);
					} else if (key.endsWith(".others2")) {
						currentothers2 = (String) commandlist.get(key);
					} else if (key.endsWith(".recipient")) {
						currentrecipient = (String) commandlist.get(key);
					} else if (key.endsWith(".help")) {
						currenthelp = (String) commandlist.get(key);
					}
				}
			}

			if (!currentcommand.equalsIgnoreCase("")) {
				EmoteMeCommand newcommand = new EmoteMeCommand();
				newcommand.name = currentcommand;
				newcommand.othertext = currentothers;
				newcommand.othertext2 = currentothers2;
				newcommand.recipienttext = currentrecipient;
				newcommand.sendertext = currentsender;
				newcommand.sendertext2 = currentsender2;
				newcommand.help = currenthelp;

				if (!emotelist.containsKey(currentcommand)) {
					emotelist.put(currentcommand, newcommand);
				} else {
				    getLogger().warning(PREFIX + " emote command '" + currentcommand + "' was already registered.");
				}
			}

		}
	}

	private void createConfig(File file) 
	{
		BufferedWriter writer = null;
		
		try{
			File dir = new File(this.getDataFolder(), "");
			dir.mkdirs();			
			
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
			writer.write("#Command list\n");
			writer.write("#Format:\n");
			writer.write("#\n");
			writer.write("#Commandname: \n");
			writer.write("#  sender: '&fWhole text to display to the player that used the command with proper capitalization. Use '' to escape apostrophes.'\n");
			writer.write("#  sender2: '&fOptional, this text will be displayed if there are additional arguments.'\n");
			writer.write("#  others: '&fWhole text to display to the other players.'\n");
			writer.write("#  others2: '&fOptional, this text will be displayed to others if there are additional arguments.'\n");
			writer.write("#  recipient: '&fOptional section to be sent to player representing the first argument if online.'\n");
			writer.write("#  help: '&fOptional section to be sent if the player shows the emote list.'\n");
			writer.write("#\n");
			writer.write("#Use %sender in the text to write the name of the player using the command\n");
			writer.write("#Use %1, %2, etc. for the arguments typed after the command.\n");
			writer.write("#The first argument (%1) will be the player to use as recipient\n");
			writer.write("#\n");
			writer.write("#\n");
			writer.write("#Example :'\n");
			writer.write("#\n");
			writer.write("#slap:\n");
			writer.write("#  sender: '&eYou&f slap yourself in the face.'\n");
			writer.write("#  sender2: '&eYou&f slap %1&f in the face.'\n");
			writer.write("#  others: '&f%sender&f slaps himself in the face.'\n");
			writer.write("#  others2: '&f%sender&f slaps %1&f in the face.'\n");
			writer.write("#  recipient: '&f%sender&f slaps &eyou&f in the face.'\n");
			writer.write("#  help: '/slap [name]'\n");
			writer.write("\n");
			
			writer.write("slap:\n");
			writer.write("  sender: '&eYou&f slap yourself in the face.'\n");
			writer.write("  sender2: '&eYou&f slap %1&f in the face.'\n");
			writer.write("  others: '&f%sender&f slaps himself in the face.'\n");
			writer.write("  others2: '&f%sender&f slaps %1&f in the face.'\n");
			writer.write("  recipient: '&f%sender&f slaps &eyou&f in the face.'\n");
			writer.write("  help: '/slap [name]'\n");
			writer.write("poke:\n");
			writer.write("  sender: '&eYou&f poke yourself.'\n");
			writer.write("  sender2: '&eYou&f poke %1&f.'\n");
			writer.write("  others: '&f%sender&f pokes himself.'\n");
			writer.write("  others2: '&f%sender&f pokes %1&f.'\n");
			writer.write("  recipient: '&f%sender&f pokes &eyou&f.'\n");
			writer.write("  help: '/poke [name]'\n");
			writer.write("punch:\n");
			writer.write("  sender: '&eYou&f punch yourself in the face.'\n");
			writer.write("  sender2: '&eYou&f punch %1&f in the face.'\n");
			writer.write("  others: '&f%sender&f punches himself in the face.'\n");
			writer.write("  others2: '&f%sender&f punches %1&f in the face.'\n");
			writer.write("  recipient: '&f%sender&f punches &eyou&f in the face.'\n");
			writer.write("  help: '/punch [name]'\n");
			writer.write("hug:\n");
			writer.write("  sender: '&eYou&f want a hug.'\n");
			writer.write("  sender2: '&eYou&f hug %1&f.'\n");
			writer.write("  others: '&f%sender&f wants a hug.'\n");
			writer.write("  others2: '&f%sender&f hugs %1&f.'\n");
			writer.write("  recipient: '&f%sender&f hugs &eyou&f.'\n");
			writer.write("  help: '/hug [name]'\n");
			writer.write("cuddle:\n");
			writer.write("  sender: '&eYou&f want to be cuddled.'\n");
			writer.write("  sender2: '&eYou&f cuddle with %1&f.'\n");
			writer.write("  others: '&f%sender&f wants to be cuddled.'\n");
			writer.write("  others2: '&f%sender&f cuddles with %1&f.'\n");
			writer.write("  recipient: '&f%sender&f cuddles with &eyou&f.'\n");
			writer.write("  help: '/cuddle [name]'\n");
			writer.write("cry:\n");
			writer.write("  sender: '&eYou&f cry crocodile tears.'\n");
			writer.write("  sender2: '&eYou&f cry on %1&f''s shoulder.'\n");
			writer.write("  others: '&f%sender&f cries crocodile tears.'\n");
			writer.write("  others2: '&f%sender&f cries on %1&f''s shoulder.'\n");
			writer.write("  recipient: '&f%sender&f cries on &eyour&f shoulder.'\n");
			writer.write("  help: '/cry [name]'\n");
			writer.write("bearhug:\n");
			writer.write("  sender: '&eYou&f want a bearhug.'\n");
			writer.write("  sender2: '&eYou&f bearhug %1&f.'\n");
			writer.write("  others: '&f%sender&f wants a bearhug.'\n");
			writer.write("  others2: '&f%sender&f bearhugs %1&f.'\n");
			writer.write("  recipient: '&f%sender&f bearhugs &eyou&f.'\n");
			writer.write("  help: '/bearhug [name]'\n");
			writer.write("pull:\n");
			writer.write("  sender: '&eYou&f pull yourself togheter.'\n");
			writer.write("  sender2: '&eYou&f pull %1&f closer.'\n");
			writer.write("  others: '&f%sender&f pulls himself togheter.'\n");
			writer.write("  others2: '&f%sender&f pulls %1&f closer.'\n");
			writer.write("  recipient: '&f%sender&f pulls &eyou&f closer.'\n");
			writer.write("  help: '/pull [name]'\n");
			writer.write("push:\n");
			writer.write("  sender: '&eYou&f push yourself beyond.'\n");
			writer.write("  sender2: '&eYou&f push %1&f away.'\n");
			writer.write("  others: '&f%sender&f pushes himself beyond.'\n");
			writer.write("  others2: '&f%sender&f pushes %1&f away.'\n");
			writer.write("  recipient: '&f%sender&f pushes &eyou&f away.'\n");
			writer.write("  help: '/push [name]'\n");
			writer.write("facepalm:\n");
			writer.write("  sender: '&eYou&f facepalm at your mistake.'\n");
			writer.write("  sender2: '&eYou&f facepalm because of %1&f.'\n");
			writer.write("  others: '&f%sender&f facepalms at his mistake.'\n");
			writer.write("  others2: '&f%sender&f facepalms because of %1&f.'\n");
			writer.write("  recipient: '&f%sender&f facepalms because of &eyou&f.'\n");
			writer.write("  help: '/facepalm [name]'\n");
			writer.write("doubleslap:\n");
			writer.write("  sender: '&eYou&f doubleslap yourself in the face.'\n");
			writer.write("  sender2: '&eYou&f doubleslap %1&f in the face.'\n");
			writer.write("  others: '&f%sender&f doubleslaps himself in the face.'\n");
			writer.write("  others2: '&f%sender&f doubleslaps %1&f in the face.'\n");
			writer.write("  recipient: '&f%sender&f doubleslaps &eyou&f in the face.'\n");
			writer.write("  help: '/doubleslap [name]'\n");
			writer.write("sigh:\n");
			writer.write("  sender: '&eYou&f let out a sigh.'\n");
			writer.write("  sender2: '&eYou&f sigh at %1&f.'\n");
			writer.write("  others: '&f%sender&f lets out a sigh.'\n");
			writer.write("  others2: '&f%sender&f sighs at %1&f.'\n");
			writer.write("  recipient: '&f%sender&f sighs at &eyou&f.'\n");
			writer.write("  help: '/sigh [name]'\n");
			writer.write("lol:\n");
			writer.write("  sender: '&eYou&f laugh out loud.'\n");
			writer.write("  sender2: '&eYou&f laugh out loud with %1&f.'\n");
			writer.write("  others: '&f%sender&f laughs out loud.'\n");
			writer.write("  others2: '&f%sender&f laughs out loud with %1&f.'\n");
			writer.write("  recipient: '&f%sender&f laughs out loud with &eyou&f.'\n");
			writer.write("  help: '/lol [name]'\n");
			writer.write("lmao:\n");
			writer.write("  sender: '&eYou&f laugh your ass off.'\n");
			writer.write("  sender2: '&eYou&f laugh your ass off with %1&f.'\n");
			writer.write("  others: '&f%sender&f laughs his ass off.'\n");
			writer.write("  others2: '&f%sender&f laughs his ass off with %1&f.'\n");
			writer.write("  recipient: '&f%sender&f laughs his ass off with &eyou&f.'\n");
			writer.write("  help: '/lol [name]'\n");
			writer.write("laugh:\n");
			writer.write("  sender: '&eYou&f laugh.'\n");
			writer.write("  sender2: '&eYou&f laugh with %1&f.'\n");
			writer.write("  others: '&f%sender&f laughs.'\n");
			writer.write("  others2: '&f%sender&f laughs with %1&f.'\n");
			writer.write("  recipient: '&f%sender&f laughs with &eyou&f.'\n");
			writer.write("  help: '/laugh [name]'\n");
			writer.write("rofl:\n");
			writer.write("  sender: '&eYou&f roll on the floor laughing.'\n");
			writer.write("  sender2: '&eYou&f roll on the floor laughing with %1&f.'\n");
			writer.write("  others: '&f%sender&f rolls on the floor laughing.'\n");
			writer.write("  others2: '&f%sender&f rolls on the floor laughing with %1&f.'\n");
			writer.write("  recipient: '&f%sender&f rolls on the floor laughing with &eyou&f.'\n");
			writer.write("  help: '/rofl [name]'\n");
			writer.write("dance:\n");
			writer.write("  sender: '&eYou&f start dancing.'\n");
			writer.write("  sender2: '&eYou&f start dancing with %1&f.'\n");
			writer.write("  others: '&f%sender&f starts dancing.'\n");
			writer.write("  others2: '&f%sender&f starts dancing with %1&f.'\n");
			writer.write("  recipient: '&f%sender&f starts dancing with &eyou&f.'\n");
			writer.write("  help: '/dance [name]'\n");
			writer.write("watch:\n");
			writer.write("  sender: '&eYou&f watch around yourself.'\n");
			writer.write("  sender2: '&eYou&f watch %1&f closely.'\n");
			writer.write("  others: '&f%sender&f watches around himself.'\n");
			writer.write("  others2: '&f%sender&f watches %1&f closely.'\n");
			writer.write("  recipient: '&f%sender&f watches &eyou&f closely.'\n");
			writer.write("  help: '/watch [name]'\n");
			writer.write("hide:\n");
			writer.write("  sender: '&eYou&f hide.'\n");
			writer.write("  sender2: '&eYou&f hide from %1&f.'\n");
			writer.write("  others: '&f%sender&f hides.'\n");
			writer.write("  others2: '&f%sender&f hides from %1&f.'\n");
			writer.write("  recipient: '&f%sender&f hides from &eyou&f.'\n");
			writer.write("  help: '/hide [name]'\n");
			writer.write("kiss:\n");
			writer.write("  sender: '&eYou&f want a kiss.'\n");
			writer.write("  sender2: '&eYou&f kiss %1&f.'\n");
			writer.write("  others: '&f%sender&f wants a kiss.'\n");
			writer.write("  others2: '&f%sender&f kisses %1&f.'\n");
			writer.write("  recipient: '&f%sender&f kisses &eyou&f.'\n");
			writer.write("  help: '/kiss [name]'\n");
			writer.write("wait:\n");
			writer.write("  sender: '&eYou&f are waiting.'\n");
			writer.write("  sender2: '&eYou&f wait for %1&f.'\n");
			writer.write("  others: '&f%sender&f is waiting.'\n");
			writer.write("  others2: '&f%sender&f waits for %1&f.'\n");
			writer.write("  recipient: '&f%sender&f waits for &eyou&f.'\n");
			writer.write("  help: '/wait [name]'\n");
			writer.write("follow:\n");
			writer.write("  sender: '&eYou&f follow the path.'\n");
			writer.write("  sender2: '&eYou&f follow %1&f.'\n");
			writer.write("  others: '&f%sender&f following a path.'\n");
			writer.write("  others2: '&f%sender&f follows %1&f.'\n");
			writer.write("  recipient: '&f%sender&f follows &eyou&f.'\n");
			writer.write("  help: '/follow [name]'\n");
			writer.write("wave:\n");
			writer.write("  sender: '&eYou&f wave to everyone.'\n");
			writer.write("  sender2: '&eYou&f waves at %1&f.'\n");
			writer.write("  others: '&f%sender&f waves to everyone.'\n");
			writer.write("  others2: '&f%sender&f waves at %1&f.'\n");
			writer.write("  recipient: '&f%sender&f waves at &eyou&f.'\n");
			writer.write("  help: '/wave [name]'\n");
			writer.write("train:\n");
			writer.write("  sender: '&eYou&f do the train.'\n");
			writer.write("  sender2: '&eYou&f do the train with %1&f.'\n");
			writer.write("  others: '&f%sender&f does the train.'\n");
			writer.write("  others2: '&f%sender&f does the train with %1&f.'\n");
			writer.write("  recipient: '&f%sender&f does the train with &eyou&f.'\n");
			writer.write("  help: '/train [name]'\n");
			writer.write("angry:\n");
			writer.write("  sender: '&eYou&f are angry.'\n");
			writer.write("  sender2: '&eYou&f are angry at %1&f.'\n");
			writer.write("  others: '&f%sender&f is angry.'\n");
			writer.write("  others2: '&f%sender&f is angry at %1&f.'\n");
			writer.write("  recipient: '&f%sender&f is angry at &eyou&f.'\n");
			writer.write("  help: '/angry [name]'\n");
			
			writer.close();
		}catch (IOException e){
		    getLogger().severe("[" + NAME + "] Unable to create config file : " + file.getName() + "!");
		    getLogger().severe(e.getMessage());
		} finally {                      
			if (writer != null) try {
				writer.close();
			} catch (IOException e2) {}
		}
	}
}
