package me.indexyz.strap;

import me.indexyz.strap.define.UserEventsKind;
import me.indexyz.strap.object.Update;
import me.indexyz.strap.utils.BotNetwork;
import me.indexyz.strap.utils.UpdateExec;

import java.util.List;

public class Bot {
    private BotNetwork network;
    private Boolean enabled;
    private static Bot instance;
    private UpdateExec execer;

    private Bot() {
    }

    public static Bot get() {
        if (Bot.instance == null) {
            throw new RuntimeException("Bot is not created");
        }

        return Bot.instance;
    }

    public void init(String token) {
        this.network = new BotNetwork(token);
        this.execer = new UpdateExec(this.network);
    }

    public static Bot create(String token) {
        Bot.instance = new Bot();
        Bot.instance.init(token);
        return get();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }


    public void start() {
        // Start pulling message
        this.enabled = true;
        long lastUpdateId = 0;

        while (this.enabled) {
            try {
                List<Update> updates = this.network.getUpdates(lastUpdateId);
                if (updates.size() != 0) {
                    lastUpdateId = updates.get(updates.size() - 1).update_id + 1;
                }

                updates.stream()
                    .forEach(update -> {
                        new Thread(() -> {
                            this.execUpdate(update);
                        }).start();
                    });
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void execUpdate(Update update) {
        if (update.message != null && update.message.text != null && update.message.text.startsWith("/")) {
            this.execer.execCommandUpdate(update);
        }

        if (update.message != null && update.message.new_chat_members != null) {
            this.execer.execUserEvent(update, UserEventsKind.JOINED_CHAT);
        }
    }
}