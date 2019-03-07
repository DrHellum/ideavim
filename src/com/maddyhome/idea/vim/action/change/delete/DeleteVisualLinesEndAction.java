/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2019 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.action.change.delete;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.action.VimCommandAction;
import com.maddyhome.idea.vim.command.Command;
import com.maddyhome.idea.vim.command.CommandFlags;
import com.maddyhome.idea.vim.command.CommandState;
import com.maddyhome.idea.vim.command.MappingMode;
import com.maddyhome.idea.vim.command.SelectionType;
import com.maddyhome.idea.vim.common.TextRange;
import com.maddyhome.idea.vim.handler.VisualOperatorActionHandler;
import com.maddyhome.idea.vim.helper.EditorHelper;
import com.maddyhome.idea.vim.helper.UtilsKt;
import com.maddyhome.idea.vim.helper.VimSelection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author vlan
 */
public class DeleteVisualLinesEndAction extends VimCommandAction {
  public DeleteVisualLinesEndAction() {
    super(new VisualOperatorActionHandler() {
      @Override
      protected boolean executeAction(@NotNull Editor editor,
                                      @NotNull Caret caret,
                                      @NotNull DataContext context,
                                      @NotNull Command cmd,
                                      @NotNull VimSelection range) {
        if (CommandState.inVisualBlockMode(editor)) {
          TextRange vimTextRange = UtilsKt.toVimTextRange(range, editor);
          final int[] starts = vimTextRange.getStartOffsets();
          final int[] ends = vimTextRange.getEndOffsets();
          for (int i = 0; i < starts.length; i++) {
            if (ends[i] > starts[i]) {
              ends[i] = EditorHelper.getLineEndForOffset(editor, starts[i]);
            }
          }
          final TextRange blockRange = new TextRange(starts, ends);
          return VimPlugin.getChange()
            .deleteRange(editor, editor.getCaretModel().getPrimaryCaret(), blockRange, SelectionType.BLOCK_WISE, false);
        }
        else {
          final TextRange lineRange = new TextRange(EditorHelper.getLineStartForOffset(editor, range.getStart()),
                                                    EditorHelper.getLineEndForOffset(editor, range.getEnd()) + 1);
          return VimPlugin.getChange().deleteRange(editor, caret, lineRange, SelectionType.LINE_WISE, false);
        }
      }
    });
  }

  @NotNull
  @Override
  public Set<MappingMode> getMappingModes() {
    return MappingMode.V;
  }

  @NotNull
  @Override
  public Set<List<KeyStroke>> getKeyStrokesSet() {
    return parseKeysSet("D");
  }

  @NotNull
  @Override
  public Command.Type getType() {
    return Command.Type.DELETE;
  }

  @Override
  public EnumSet<CommandFlags> getFlags() {
    return EnumSet.of(CommandFlags.FLAG_MOT_LINEWISE, CommandFlags.FLAG_EXIT_VISUAL);
  }
}
