package ru.nsu.problem_forge.type.problem

enum class SolutionType(val value: String = "", val color: String = "RED") {
    MAIN_AC("Main correct solution", "GREEN"),
    AC("Correct", "GREEN"),
    INCORRECT("Incorrect"),

    TLE( "Time limit exceeded"),
    TLE_OR_AC("Time limit exceeded or correct"),
    TLE_OR_ML("Time limit exceeded or Memory limit exceeded"),
    WA("Wrong answer"),
    PE("Presentation error"),
    ML("Memory limit exceeded"),
    FAILED ("Failed"),
    DO_NOT_RUN("Do not run");
}