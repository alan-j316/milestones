# Milestones

A simple weight-tracking Android application built with legacy XML-based layouts and Java.
Milestones is tailored for users who want to closely monitor their weight loss or gain journey. It
provides intuitive interactions, tracks daily logs via grid lists, and offers vital encouragement
through at-a-glance progress bars. Because weight changes take significant time to become
noticeable, the app also features an optional SMS notification system to send congratulatory
messages when users reach their goals.

*Note: This project was developed as a part of SNHU's CS360 course.*

## Screenshots

![App Screenshot 1](assets/first-screenshots.png)

![App Screenshot 2](assets/second-screenshots.png)

![App Screenshot 3](assets/third-screenshots.png)



## How to run

1. Clone or download the project
2. Install [Android Studio](https://developer.android.com/studio) if you don't have it installed
   already.
3. Open the project and run
4. Alternatively, you can download the APK from the releases section.

**Make sure you have Android Studio Quail | 2026.1.2 or later as this project uses AGP 9.3.3**

## Details

### Requirements and Goals

The primary goal of Milestones was to create a functional, persistent CRUD application using an
SQLite database that addresses the core needs of a user tracking their physical health. The app was
designed to address three specific user needs: a frictionless data-logging experience, visual
encouragement over long periods, and reliable data persistence across sessions (including persistent
user authentication).

### User-Centered UI Design

To keep the interface clean and minimal, the application is divided into distinct, purpose-driven
screens:

* **Authentication**: Split into separate Login and Register screens to avoid the confusion of a
  single page with multiple primary actions. It includes UX polishes like password visibility
  toggles and clear input validation hints. A dedicated screen for users to establish their
  baseline (name, current weight, goal weight).

* **Home (Tracking)**: Features an "at-a-glance" visual progress bar alongside a grid list (
  RecyclerView) of logged weights. This solves the UX problem of users having to sift through raw
  data to understand their trajectory.

* **Profile**: Allows users to dynamically update their goals (accommodating changing fitness
  targets) and toggle the optional SMS feature.

* **Empty States**: Instead of staring at a blank screen upon first login, the Home screen provides
  an engaging empty state layout encouraging the user to log their first weight.

These designs were successful because they were driven by a user persona, ensuring the application
remains an encouraging tool rather than a tedious ledger.

### Coding Approach and Strategies

The development process followed a UI-first approach, moving sequentially from visual layouts to
data models, and finally to the controller layers. To ensure modularity, I utilized the Repository
pattern to create a unified interface for data access (SQLite database and SharedPreferences),
keeping the Activity classes lightweight. I applied a vertical slice strategy, building and
perfecting one feature end-to-end before moving to the next. This acted as a strict guardrail
against scope creep.

In the future, while these foundational architectural concepts remain highly applicable, I would
transition this logic to modern toolkits like Kotlin, Jetpack Compose, and Room. Leveraging Kotlin's
Flows and Coroutines would significantly reduce boilerplate click-listener code, handle asynchronous
database queries seamlessly off the main thread, and decouple the UI even further.


### Credits
Illustrations by undraw: https://undraw.co/illustrations