package com.cs360.weighttracker.presentation.utils;

/**
 * Interface for passing around click listeners in function.
 */
@FunctionalInterface
public interface ErrorResetCallback {
    void onCallback();
}