# Contribute to mTHMMY

Thank you for your interest in contributing to mTHMMY! This guide details how
to contribute to mTHMMY in a way that is efficient for everyone.

## Security vulnerability disclosure

**Important!** Instead of creating publicly viewable issues for suspected security
vulnerabilities, please report them in private to
[thmmynolife@gmail.com](mailto:thmmynolife@gmail.com).

## I want to contribute!

There are many ways of contributing to mTHMMY:

- Simply using the [latest release version][google-play] from Google Play (anonymous reports are sent automatically)
- Joining our [Discord server][discord-server]
- Submitting bugs and ideas to our [issue tracker][github-issues]
- Forking mTHMMY and submitting [pull requests](#pull-requests)
- Joining our core team
- Contacting us by email at [thmmynolife@gmail.com](mailto:thmmynolife@gmail.com)

## Issue tracker

For bugs and improvements we use [GitHub’s issue tracking][github-issues].
Before creating a new issue make sure to **search the tracker** for similar ones.

## Compiling

Due to the app's integration with Firebase, a *google-services.json* file is required inside the *app* directory. To get one, either [set up your own Firebase project][firebase-console] (with or without a self hosted [backend][sisyphus]), or ask us to provide you the one we use for development.

## Pull requests

Pull requests with fixes and improvements to mTHMMY are most welcome. Any developer that wants to work independently from the core team can simply
follow the workflow below to make a pull request (PR):

1. Fork the project into your personal space on Github
1. Create a feature branch, away from [develop](https://github.com/ThmmyNoLife/mTHMMY/tree/develop)
1. Push the commit(s) to your fork
1. Create a PR targeting [develop at mTHMMY](https://github.com/ThmmyNoLife/mTHMMY/tree/develop)
1. Fill the PR title describing the change you want to make
1. Fill the PR description with a brief motive for your change and the method you used to achieve it
1. Submit the PR.

[google-play]: https://play.google.com/store/apps/details?id=gr.thmmy.mthmmy
[github-issues]: https://github.com/ThmmyNoLife/mTHMMY/issues
[discord-server]: https://discord.gg/CVt3yrn
[sisyphus]: https://github.com/ThmmyNoLife/Sisyphus
[firebase-console]: https://console.firebase.google.com/
