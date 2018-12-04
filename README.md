# Kitsune

Very fox microblogging.

## Configuration

### config.edn

You should pass in a config file using system props. For example if it's called `config.edn`, then the Kitsune launch command would be

```
java -jar kitsune.jar -Dconf=config.edn
```

Refer to [the sample in the repo](https://github.com/valerauko/kitsune/blob/master/config/prod/config.edn) on what its format is.

### Environment variables

If you don't want to store your database credentials in an unencrypted file, you can use environment variables too (note the double underscores):

* `DB__HOST` (default `localhost`)
* `DB__DB` (default `kitsune`)
* `DB__USER` (default `kitsune`)
* `DB__PASS`

## License

Copyright (C) 2018 @[valerauko](https://github.com/valerauko)

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program. If not, see https://www.gnu.org/licenses/.

### Additional permission under GNU AGPL section 7:

If you modify this Program, or any covered work, by linking or combining it with its dependencies  (or a modified version thereof) as listed in [project.clj](project.clj), containing parts covered by the terms of the dependencies' respective licenses, the licensors of this Program grant you additional permission to convey the resulting work.

Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the dependencies used as well as that of the covered work.
