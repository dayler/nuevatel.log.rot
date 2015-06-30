rename table `$schema$`.`$tableName$_1` to `$schema$`.`$tableName$_$suffix$`;
create table `$schema$`.`$tableName$_0_new` like `$schema$`.`$tableName$_0`;
rename table `$schema$`.`$tableName$_0` to `$schema$`.`$tableName$_1`, `$schema$`.`$tableName$_0_new` to `$schema$`.`$tableName$_0`;
