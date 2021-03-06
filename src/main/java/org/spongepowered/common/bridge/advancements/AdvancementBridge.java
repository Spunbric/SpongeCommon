/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.bridge.advancements;

import net.minecraft.advancements.Advancement;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.Translation;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public interface AdvancementBridge {

    ResourceKey bridge$getKey();

    Translation bridge$getTranslation();

    void bridge$setTranslation(Translation translation);

    Optional<Advancement> bridge$getParent();

    void bridge$setParent(@Nullable Advancement advancement);

    Optional<AdvancementTree> bridge$getTree();

    void bridge$setTree(AdvancementTree tree);

    AdvancementCriterion bridge$getCriterion();

    void bridge$setCriterion(AdvancementCriterion criterion);

    boolean bridge$isRegistered();

    void bridge$setRegistered();

    Text bridge$getText();

    List<Text> bridge$getToastText();
}
