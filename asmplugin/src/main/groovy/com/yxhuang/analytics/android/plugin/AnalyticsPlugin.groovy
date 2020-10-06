package com.yxhuang.analytics.android.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AnalyticsPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        AnalyticsExtension extension = project.extensions.create("analytics", AnalyticsExtension)
        println("------------AnalyticsPlugin apply  extension disableAutoTrack --------------" +  extension.disableAutoTrack)

        AppExtension appExtension = project.extensions.findByType(AppExtension.class)
        appExtension.registerTransform(new AnalyticsTransform(extension))
    }
}