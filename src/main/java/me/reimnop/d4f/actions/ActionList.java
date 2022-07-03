package me.reimnop.d4f.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ActionList {
    private static class ActionValuePair {
        public Action action;
        public JsonElement value;

        public ActionValuePair(Action action, JsonElement value) {
            this.action = action;
            this.value = value;
        }
    }

    private final List<ActionValuePair> actions = new ArrayList<>();

    public ActionList(JsonArray jsonArray) {
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            actions.add(new ActionValuePair(ModActions.get(jsonObject.get("id").getAsString()), jsonObject.get("value")));
        }
    }

    public void runActions() {
        for (ActionValuePair pair : actions) {
            pair.action.runAction(pair.value);
        }
    }
}
