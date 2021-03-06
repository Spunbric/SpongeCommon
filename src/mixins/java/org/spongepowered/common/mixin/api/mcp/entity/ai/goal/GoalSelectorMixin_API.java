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
package org.spongepowered.common.mixin.api.mcp.entity.ai.goal;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import org.spongepowered.api.entity.ai.goal.Goal;
import org.spongepowered.api.entity.ai.goal.GoalExecutor;
import org.spongepowered.api.entity.ai.goal.GoalExecutorType;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.ai.GoalBridge;
import org.spongepowered.common.bridge.entity.ai.GoalSelectorBridge;

import java.util.List;
import java.util.Set;

@Mixin(GoalSelector.class)
public abstract class GoalSelectorMixin_API<O extends Agent> implements GoalExecutor<O> {

    @Shadow @Final private Set<PrioritizedGoal> goals;

    @Shadow public abstract void shadow$addGoal(int priority, net.minecraft.entity.ai.goal.Goal task);
    @Shadow public abstract void shadow$removeGoal(net.minecraft.entity.ai.goal.Goal task);

    @SuppressWarnings("unchecked")
    @Override
    public O getOwner() {
        return (O) ((GoalSelectorBridge) this).bridge$getOwner();
    }

    @Override
    public GoalExecutorType getType() {
        return ((GoalSelectorBridge) this).bridge$getType();
    }

    @Override
    public GoalExecutor<O> addGoal(final int priority, final Goal<? extends O> task) {
        this.shadow$addGoal(priority, (net.minecraft.entity.ai.goal.Goal) task);
        return this;
    }

    @Override
    public GoalExecutor<O> removeGoal(final Goal<? extends O> goal) {
        this.shadow$removeGoal((net.minecraft.entity.ai.goal.Goal) goal);
        return  this;
    }

    @Override
    public GoalExecutor<O> removeGoals(final GoalType type) {
        this.goals.removeIf(goal -> ((GoalBridge)goal.getGoal()).bridge$getType() == type);
        return this;
    }

    @Override
    public List<? super Goal<? extends O>> getTasksByType(final GoalType type) {
        final ImmutableList.Builder<Goal<?>> tasks = ImmutableList.builder();
        this.goals.stream().map(PrioritizedGoal::getGoal).map(Goal.class::cast)
                .filter(goal -> goal.getType() == type)
                .forEach(tasks::add);
        return tasks.build();
    }

    @Override
    public List<? super Goal<? extends O>> getTasks() {
        final ImmutableList.Builder<Goal<?>> tasks = ImmutableList.builder();
        this.goals.stream().map(PrioritizedGoal::getGoal).map(Goal.class::cast).forEach(tasks::add);
        return tasks.build();
    }

    @Override
    public void clear() {
        this.goals.clear();
    }
}
