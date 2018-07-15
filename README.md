## SBT Docker Compose plugin

`sbt-docker-compose` plugin provides ultimate solution for running integration tests against docker containers with health checking support.

Plugin consists of three runnable tasks: `dockerCompose`, `dockerComposeTest`, `dockerComposeTestQuick` which will be discussed in the next sections.

#### Comparison to similar projects

Tapad's [sbt-docker-compose](https://github.com/Tapad/sbt-docker-compose) plugin is the alternative to this plugin, it's main downside is using sbt `commands` instead of `tasks` which makes it ineffcicient in multi-module projects.
Also it doesn't expose `docker-compose` commands as it's public API, so you cannot modify options passed to `docker-compose` cli.

On the contrary `sbt-docker-compose` plugin is using tasks, exposes docker compose comamnds as it's public API, runs health check tests before running integration tests, provides containers host/port as system property, uses SBT's built in task for running tests/it-test and so many other features.

| Features                                                 | SBT docker compose plugin | Tapad's SBT docker compose plugin |
|----------------------------------------------------------|---------------------------|-----------------------------------|
| Multiproject support                                     | Yes                       | No                                |
| Health check support                                     | Yes                       | No                                |
| Testframework agnostic                                   | Yes                       | No                                |
| Tag substitution                                         | Yes                       | No                                |
| Container host/port as SystemProperty                    | Yes                       | No                                |
| Docker Compose commands as configurable SBT Setting keys | Partially                 | No                                |
| Autocompletion support                                   | Yes                       | No                                |

#### How to Setup/Enable?

1 - Add the `sbt-docker-compose` plugin to the plugins.sbt file:
```scala
addSbtPlugin("com.github.ehsanyou" % "sbt-docker-compose" % "1.1.0")
```
2 - Enable the auto-plugin on your desired sbt project:
```scala
enablePlugins(DockerCompose)
```

#### Race conditions

In order to prevent race conditions caused by identical `docker-compose` project name in CI nodes, a new option `--project-name-suffix` has been introduced, value of this option will append to the final project name.

#### How to use?

Plugin consists of three runnable tasks `dockerCompose`, `dockerComposeTest`, `dockerComposeTestQuick`.

##### `dockerCompose` :

Behaves exactly like `docker-compose` cli, takes options like `--project-name` with autocomplete support, the only difference is limitiations on supported commands.

It only supports `up` and `down` commands, the aim is to facilate running integration test against docker containers, aformentioned commands cover majority of use cases.

In addition to that `dockerCompose` task takes an extra optional option: `--tags` which is part of tag subsitution feature that will be explained later.

**Task structure**:
```sbt
// dockerCompose [docker-compose options i.e: -p] [docker-compose commands [up, down]] [docker-compose commands options]
dockerCompose -p myproject up -d --tags myservice:new-image-tag
```
Using SBT's autocomplete feature is the easies way to experiment.

##### `dockerComposeTest` :

It runs containers defined in the `docker-compose` file, provides their host/port as System properties, so they are easily accessable in tests.

Then it waits 'till containers return healthy and runs tests/it-tests depend on how its parameterized, regardless of the test result, it will shutdown the containers and returns with proper status code.

It also supports tag susbstitution same with `dockerCompose` task.

If you want to see container logs while running integration tests configure `dockerComposeTestLogging` setting key on your sbt project.

```sbt
dockerComposeTestLogging := true // default value is false
```

**Task structure**:
```sbt
// dockerComposeTest [docker-compose options i.e -p] [test/testOnly/testQuick/it:test,...] [docker-compose up command options: i.e --force-recreate]
dockerComposeTest --no-ansi it:testOnly "*MyClass" --force-recreate
```

##### `dockerComposeTestQuick` :

It's the same with `dockerComposeTest` except that it skips shutting down the booted containers.

It helps to save some time during development i.e in cases when it's required to run integration tests multiple times against same set of containers.

#### Default Behaviour

It's possible to provide default behaviour for supported commands [up, down, test], so it's not required to pass options to the task every time it's invoked.

```sbt

    dockerComposeCommandOptions :=
        DockerComposeCmd()
            .withOption(("-p", "projectname))

    dockerComposeUpCommandOptions := 
        DockerComposeUpCmd()
            .withOption("-d")
            
    dockerComposeDownCommandOptions := 
        DockerComposeDownCmd()
            .withOption("--remove-orphans")
            
    /* 
      `dockerComposeTestCommand` is not part of docker-compose api,
      because dockerComposeTest runs `docker-compose up` eventually 
      you can configure options passed to `docker-compose up`
    */
    
    dockerComposeTestCommandOptions := 
        DockerComposeTestCmd()
            .withOption(("-p", "myproject"))

```

So next time if i.e `dockerCompose up` gets invoked it will fallback to defined default options.

#### Health Check

`dockerComposeTest` command as stated before boots up containers and checks for health status of containers in `500ms` interval, if all containers with health status support report `healthy` status it runs the tests.
Notice: Plugin omits containers without health check([HealthCheck instruction](https://docs.docker.com/engine/reference/builder/#healthcheck)) status.

#### Image tag substitution

`dockerCompose .. up ..`, `dockerComposeTest`, `dockerComposeQuickTest` tasks support an extra option `--tags service-name:image-tag, ...`, this plugin uses this option to substitute image tags defined in the docker-compose file on the fly, so it's possible to run integration tests against different versions of services.
Also there's a SBT setting key called `dockerComposeTags` you append tags you want to substitute to it, i.e :
```sbt
    dockerComposeTags += ("service-name", "tag-name")
```
tags provided by cli supersede this option.

#### Container's ip/port as system property

In order to run integration test against running instance of the services their host/port address are required.
i.e if the service name in docker-compose file is `foobar` and scale number of the service is `1` and the host port is `1234` you can get the public interface and container with following key:
`foobar_1_1234`.

You can also access host name and public port of the container individually, just append `_host` or `_port` to the property key.

```scala
System.getProperty("foobar_1_1234") -> "interface:port" // foobar is the service name defined in docker-compose file, 1 is scale number and 1234 is host port of the container
System.getProperty("foobar_1_1234_host") -> "interface"
System.getProperty("foobar_1_1234_port") -> "port"
```

Since in most cases services expose only one port, and are not replicated you can access service host/port using service name only.

```scala
System.getProperty("foobar") -> "interface:port"
System.getProperty("foobar_host") -> "interface"
System.getProperty("foobar_port") -> "port"
```

If you can't guess the key just print all system properties, and find your key.


#### Configuration

There are a number of optional keys that can be set as well if you want to override the default setting.
```scala
    // Ignores all provided tasks in project scope ( useful for root projects in multi-project configuration )"
    dockerComposeIgnore := ???
    // Possible docker-compose file paths 
    dockerComposeFilePaths := ???
    dockerComposeHealthCheckDeadline := // Overall deadline for health-checking
    dockerComposeTags := // List of services and and their respective tag you'd like to override, --tags option in cli will supersede tags defined in this setting key.
    dockerComposeProjectName := 
    /* 
      Plugin runs you container in isolation,
      it does the isolation with help of `docker-compose` `-p` option.
      Plugin uses value of this setting as a default value to `-p` option.
     */

``` 

#### Concurrency Level

On multi-project config with too many projects is better to limit concurrency level of `dockerComposeTest` task, if you don't you'll get some crazy errors from `docker-compose`.

```scala
concurrentRestrictions in Global := Seq(
  Tags.limit(DockerComposeTest, 3)
)
```

Here I limited concurrency level of tasks using `DockerComposeTest` tag to `3`.

