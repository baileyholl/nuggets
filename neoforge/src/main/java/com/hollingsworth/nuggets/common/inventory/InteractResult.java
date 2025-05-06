package com.hollingsworth.nuggets.common.inventory;

/**
 * Returns the preference of the given stack, and whether it is valid for a given action.
 * If Valid is false, the action should not be performed.
 */
public record InteractResult(SortPref sortPref, boolean valid) {
}
