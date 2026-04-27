package software.coley.gradle.buildservice

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class SimpleBuildService implements BuildService<BuildServiceParameters.None> {
}
