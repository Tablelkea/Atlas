package com.veloriastudio.atlas.api.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class DialogBuilder {

    private final Component title;
    private ActionButton confirmButton;
    private ActionButton cancelButton;

    public DialogBuilder(Component title){
        this.title = Objects.requireNonNull(title, "title cannot be null");
    }
    private final List<DialogBody> bodies = new ArrayList<>();

    private final List<DialogInput> inputs = new ArrayList<>();

    public static DialogBuilder create(Component title){

        Objects.requireNonNull(title, "title cannot be null");

        return new DialogBuilder(title);
    }

    private void validateKey(String key){
        Objects.requireNonNull(key, "key cannot be null");
        if(key.isBlank()){
            throw new IllegalArgumentException("key cannot be blank");
        }
        if(key.equals("id")){
            throw new IllegalArgumentException("key cannot be 'id'");
        }
    }

    public DialogBuilder body(DialogBody body){

        Objects.requireNonNull(body, "body cannot be null");
        bodies.add(body);
        return this;
    }

    public DialogBuilder input(DialogInput input){

        Objects.requireNonNull(input, "input cannot be null");
        inputs.add(input);
        return this;
    }

    public DialogBuilder text(String key, Component label){
        Objects.requireNonNull(label, "label cannot be null");
        validateKey(key);

        DialogInput input = DialogInput.text(key, label)
                .width(300)
                .maxLength(256)
                .build();
        inputs.add(input);
        return this;
    }

    public DialogBuilder text(String key, Component label, String initial){

        validateKey(key);
        Objects.requireNonNull(label, "label cannot be null");
        Objects.requireNonNull(initial, "initial cannot be null");

        int maxLength = Math.max(
                256,
                initial.length()
        );

        DialogInput input = DialogInput.text(key, label)
                .width(300)
                .maxLength(maxLength)
                .initial(initial)
                .build();

        inputs.add(input);
        return this;
    }

    public DialogBuilder toggle(String key, Component label, boolean initial){
        validateKey(key);
        Objects.requireNonNull(label, "label cannot be null");
        Objects.requireNonNull(initial, "initial cannot be null");

        DialogInput input = DialogInput.bool(key, label)
                .initial(initial)
                .build();
        inputs.add(input);
        return this;
    }

    public DialogBuilder number(String key, Component label, float start, float end, float initial, float step){
        validateKey(key);
        Objects.requireNonNull(label, "label cannot be null");

        if(start > end){
            throw new IllegalArgumentException("start cannot be higher than end");
        }

        if (initial < start || initial > end) {
            throw new IllegalArgumentException(
                    "initial must be between start and end"
            );
        }

        if(step < 0){
            throw new IllegalArgumentException("step must be greater then 0");
        }

        DialogInput input = DialogInput.numberRange(key, label, start, end).initial(initial).step(step).build();
        inputs.add(input);
        return this;
    }

    public DialogBuilder textArea(String key, Component label, String initial){
        validateKey(key);
        Objects.requireNonNull(label, "label cannot be null");
        Objects.requireNonNull(initial, "initial cannot be null");


        int maxLength = Math.max(
                256,
                initial.length()
        );

        DialogInput input = DialogInput.text(key, label).initial(initial).width(300).maxLength(maxLength).multiline(TextDialogInput.MultilineOptions.create(10, 120)).build();
        inputs.add(input);
        return this;
    }

    public DialogBuilder confirm(Component label, DialogActionCallback callback){
        Objects.requireNonNull(label, "label cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        DialogAction action = DialogAction.customClick(callback, ClickCallback.Options.builder()
                .uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build());
        confirmButton = ActionButton.create(label, null, 150, action);
        return this;
    }

    public DialogBuilder cancel(Component label){
        Objects.requireNonNull(label, "label cannot be null");

        DialogAction action = DialogAction.customClick(null, ClickCallback.Options.builder().uses(1).lifetime(ClickCallback.DEFAULT_LIFETIME).build());
        cancelButton = ActionButton.create(label, null, 150, action);
        return this;
    }

    public Dialog build(){
        if (confirmButton == null) {
            throw new IllegalStateException(
                    "confirm button must be defined"
            );
        }

        if (cancelButton == null) {
            throw new IllegalStateException(
                    "cancel button must be defined"
            );
        }

        DialogBase base = DialogBase.builder(title)
                .body(List.copyOf(bodies))
                .inputs(List.copyOf(inputs))
                .build();

        return Dialog.create(builder ->
                builder.empty()
                        .base(base)
                        .type(
                                DialogType.confirmation(
                                        confirmButton,
                                        cancelButton
                                )
                        )
        );
    }

    public void open(Player player) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        player.showDialog(
                build()
        );
    }

}
