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
package org.spongepowered.common.mixin.api.mcp.scoreboard;

import net.minecraft.network.play.server.SDisplayObjectivePacket;
import net.minecraft.network.play.server.SScoreboardObjectivePacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.scoreboard.ScoreBridge;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.accessor.scoreboard.ScorePlayerTeamAccessor;
import org.spongepowered.common.accessor.scoreboard.ScoreboardAccessor;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("SuspiciousMethodCalls")
@Mixin(ServerScoreboard.class)
@Implements(@Interface(iface = org.spongepowered.api.scoreboard.Scoreboard.class, prefix = "scoreboard$"))
public abstract class ServerScoreboardMixin_API extends Scoreboard {

    @Shadow protected abstract void shadow$markSaveDataDirty();

    // Get Objective

    public Optional<Objective> scoreboard$getObjective(final String name) {
        final ScoreObjective objective = this.getObjective(name);
        return Optional.ofNullable(objective == null ? null : ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    public void scoreboard$addObjective(final Objective objective) {
        final ScoreObjective nmsObjective = this.getObjective(objective.getName());

        if (nmsObjective != null) {
            throw new IllegalArgumentException("An objective with the name \'" + objective.getName() + "\' already exists!");
        }
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        List<ScoreObjective> objectives = ((ScoreboardAccessor) this).accessor$getScoreObjectiveCriterias().get(objective.getCriterion());
        if (objectives == null) {
            objectives = new ArrayList<>();
            ((ScoreboardAccessor) this).accessor$getScoreObjectiveCriterias().put((ScoreCriteria) objective.getCriterion(), objectives);
        }

        objectives.add(scoreObjective);
        ((ScoreboardAccessor) this).accessor$getScoreObjectives().put(objective.getName(), scoreObjective);
        this.onObjectiveAdded(scoreObjective);

        ((SpongeObjective) objective).updateScores(this);
    }

    // Update objective in display slot

    public void scoreboard$updateDisplaySlot(@Nullable final Objective objective, final DisplaySlot displaySlot) throws IllegalStateException {
        if (objective != null && !objective.getScoreboards().contains(this)) {
            throw new IllegalStateException("Attempting to set an objective's display slot that does not exist on this scoreboard!");
        }
        final int index = ((SpongeDisplaySlot) displaySlot).getIndex();
        ((ScoreboardAccessor) this).accessor$getObjectiveDisplaySlots()[index] = objective == null ? null: ((SpongeObjective) objective).getObjectiveFor(this);
        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SDisplayObjectivePacket(index, ((ScoreboardAccessor) this).accessor$getObjectiveDisplaySlots()[index]));
    }

    public Optional<Objective> scoreboard$getObjective(final DisplaySlot slot) {
        final ScoreObjective objective = ((ScoreboardAccessor) this).accessor$getObjectiveDisplaySlots()[((SpongeDisplaySlot) slot).getIndex()];
        if (objective != null) {
            return Optional.of(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return Optional.empty();
    }

    public Set<Objective> scoreboard$getObjectivesByCriterion(final Criterion criterion) {
        if (((ScoreboardAccessor) this).accessor$getScoreObjectiveCriterias().containsKey(criterion)) {
            return ((ScoreboardAccessor) this).accessor$getScoreObjectiveCriterias().get(criterion).stream()
                    .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    // Get objectives

    public Set<Objective> scoreboard$getObjectives() {
        return ((ScoreboardAccessor) this).accessor$getScoreObjectives().values().stream()
                .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective())
                .collect(Collectors.toSet());
    }

    @SuppressWarnings({"rawtypes"})
    public void scoreboard$removeObjective(final Objective objective) {
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        ((ScoreboardAccessor) this).accessor$getScoreObjectives().remove(scoreObjective.getName());

        for (int i = 0; i < 19; ++i)
        {
            if (this.getObjectiveInDisplaySlot(i) == scoreObjective)
            {
                //noinspection ConstantConditions
                this.setObjectiveInDisplaySlot(i, null);
            }
        }

        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SScoreboardObjectivePacket(scoreObjective, Constants.Scoreboards.OBJECTIVE_PACKET_REMOVE));

        final List list = ((ScoreboardAccessor) this).accessor$getScoreObjectiveCriterias().get(scoreObjective.getCriteria());

        if (list != null)
        {
            list.remove(scoreObjective);
        }

        for (final Map<ScoreObjective, Score> scoreMap : ((ScoreboardAccessor) this).accessor$getEntitiesScoreObjectives().values()) {
            final Score score = scoreMap.remove(scoreObjective);
            if (score != null) {
                ((ScoreBridge) score).bridge$getSpongeScore().removeScoreFor(scoreObjective);
            }
        }

        // We deliberately don't call func_96533_c, because there's no need
        this.shadow$markSaveDataDirty();

        ((SpongeObjective) objective).removeObjectiveFor(this);
    }

    public Optional<Team> scoreboard$getTeam(final String name) {
        return Optional.ofNullable((Team) ((ScoreboardAccessor) this).accessor$getTeams().get(name));
    }

    @SuppressWarnings({"unchecked"})
    public Set<Team> scoreboard$getTeams() {
        return new HashSet<>((Collection<Team>) (Collection<?>) ((ScoreboardAccessor) this).accessor$getTeams().values());
    }

    @SuppressWarnings("deprecation")
    public Optional<Team> scoreboard$getMemberTeam(final Text member) {
        return Optional.ofNullable((Team) ((ScoreboardAccessor) this).accessor$getTeamMemberships().get(SpongeTexts.toLegacy(member)));
    }

    // Add team

    public void scoreboard$registerTeam(final Team spongeTeam) {
        final ScorePlayerTeam team = (ScorePlayerTeam) spongeTeam;
        //noinspection ConstantConditions
        if (this.getTeam(spongeTeam.getName()) != null) {
            throw new IllegalArgumentException("A team with the name \'" +spongeTeam.getName() + "\' already exists!");
        }

        if (((ScorePlayerTeamAccessor) team).accessor$getScoreboard() != null) {
            throw new IllegalArgumentException("The passed in team is already registered to a scoreboard!");
        }

        ((ScorePlayerTeamAccessor) team).accessor$setScoreboard(this);
        ((ScoreboardAccessor) this).accessor$getTeams().put(team.getName(), team);

        for (final String entry: team.getMembershipCollection()) {
            this.addPlayerToTeam(entry, team);
        }
        this.onTeamAdded(team);
    }

    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores() {
        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: ((ScoreboardAccessor) this).accessor$getScoreObjectives().values()) {
            scores.addAll(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScores().values());
        }
        return scores;
    }

    public Set<org.spongepowered.api.scoreboard.Score> scoreboard$getScores(final Text name) {
        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: ((ScoreboardAccessor) this).accessor$getScoreObjectives().values()) {
            ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScore(name).ifPresent(scores::add);
        }
        return scores;
    }

    public void scoreboard$removeScores(final Text name) {
        for (final ScoreObjective objective: ((ScoreboardAccessor) this).accessor$getScoreObjectives().values()) {
            final SpongeObjective spongeObjective = ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
            spongeObjective.getScore(name).ifPresent(spongeObjective::removeScore);
        }
    }
}
