/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_20_5 extends ProtocolStorablesBase {

    private final ArmorTrimStorage armorTrims = new ArmorTrimStorage();
    private AcknowledgedMessagesStorage acknowledgedMessages = new AcknowledgedMessagesStorage();
    private ScoreboardTeamStorage scoreboardTeams = new ScoreboardTeamStorage();
    private BannerPatternStorage bannerPatterns;
    private TagKeys tagKeys;

    public AcknowledgedMessagesStorage acknowledgedMessages() {
        return acknowledgedMessages;
    }

    public void setAcknowledgedMessages(final AcknowledgedMessagesStorage acknowledgedMessages) {
        this.acknowledgedMessages = acknowledgedMessages;
    }

    public ArmorTrimStorage armorTrims() {
        return armorTrims;
    }

    public ScoreboardTeamStorage scoreboardTeams() {
        return scoreboardTeams;
    }

    public void setScoreboardTeams(final ScoreboardTeamStorage scoreboardTeams) {
        this.scoreboardTeams = scoreboardTeams;
    }

    public BannerPatternStorage bannerPatterns() {
        return bannerPatterns;
    }

    @SuppressWarnings("unused")
    public void setBannerPatterns(final BannerPatternStorage bannerPatterns) {
        this.bannerPatterns = bannerPatterns;
    }

    public TagKeys tagKeys() {
        return tagKeys;
    }

    public void setTagKeys(final TagKeys tagKeys) {
        this.tagKeys = tagKeys;
    }
}
