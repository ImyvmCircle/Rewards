# Rewards
Rewards for Imyvm Server's Daily reward, Weekly reward or other rewards.

## Commands
    /rw add [reward_name] [commands]
    ie. /rw add daily_reward eco#give#{player}#<{random}*5>:give#{player}#stone#1
[For op] You can use ":" to split multiple commands, as space is not supported, please change the the space into "#", it also supports math operations, ie. *, /, ..., just including these elements satrt with "<" and end eith ">". Where {random} means (0~1)*range+Mini. "range" and "Mini" can be modified in the config.

    /rw give [player] [reward_name]
[For op] You can give special player a reward.

    /rw ac [random code]
[For players] When a player received a reward, it will generate a random code for acquiring the reward.

