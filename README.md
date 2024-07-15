# Google Search Results - Kotlin

> A simple library that provides an API to fetch Google Search results given a query to search

[![](https://jitpack.io/v/shubham0204/google-search-kotlin.svg)](https://jitpack.io/#shubham0204/google-search-kotlin)

## Setup

The library is distributed with Jitpack. In the root `build.gradle` file, add the `jitpack` repository,

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Next, add the library dependency in the module-level `build.gradle`,

```groovy
dependencies {
    implementation 'com.github.shubham0204:google-search-kotlin:0.0.1'
}
```

## Usage

The library provides two static methods to fetch Google Search results - `search` and `searchAsFlow`. The arguments passed to both the methods are same, except that `searchAsFlow` returns a `kotlinx.coroutines.flow.Flow` object. 


```kotlin
CoroutineScope(Dispatchers.Default).launch {
    val results: Flow<GoogleSearchProvider.GoogleSearchResult> = GoogleSearchProvider.searchAsFlow(
        term  = "" ,
        readPageText = false ,
        numResults = 10,
        lang = "en",
        safe = "active",
        timeframe = GoogleSearchProvider.SearchTimeframe.PAST_24HOURS,
        readPageText = false
    )
    results.collect {
        println( it.title )
        println( it.href )
    }
}
```