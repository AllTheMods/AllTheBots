# ARS GUIDE

Format: 
```ruby
<triggername> = {<function>: <arguments>}
```
There can be an infinite amount of functions per trigger.
Trigger names have to be unique.

*note: `&...` means it's a function*    

### Functions:
```ini
[message]
Sends a message.
Possible arguments:
- text
- &message.react
- &message.delete

[dm]
Sends the user a DM
Possible arguments:
- text

[react]
Reacts to the users message.
Possible arguments:
- emote

[delete]
Deletes the original message.
Possible arguments:
- time (in seconds)

[embed]
Sends an embed.
Possible arguments:
- &field
- &title
- &description
- &color
- &message.react
- &message.delete

[role.add]
Gives a role to the user.
Possible arguments:
- role name

[role.remove]
Removes a role from the user.
Possible arguments:
- role name

```

### SubFunctions
```ini
[message.react]
See &react.

[message.react]
See &delete.

[field]
Adds a field to the embed.
Note: Used as `field[0]` (not inline) or `field[1]` (inline)
Possible arguments:
- Name|Value

[title]
Sets the embed title.
Possible arguments:
- Title

[description]
Sets the embed description.
Possible arguments:
- Description

[color]
Sets the embed color.
Possible arguments:
- Color (Hexadecimal, i.e. 000000 to FFFFFF)
```

### Conditional
Some functions are conditionals or checkers. These are usually top-level, but may appear as subfunctions.
```ini
[if]
Possible arguments:
- any &condition
- functions
- "else"
- functions if function is false

[message.contains]
Can be inverted by prepending !
Possible arguments:
- text
```

### Variables
There are some variables you can use.
```ruby
%.user                  Username of ARS user.
%.channel               Channel name.
%.server                Server name.

%.userNickname          User's nickname. If they don't have one,
                        their username is used.
%.userColor             Color of the current user.
%.userMention           Mentions the user.
%.channelDescription    The channel description.
%.channelMention        Mentions the channel.
%.topRole               Name of the user's highest role.
```

### Examples:
Poll:
```ruby
poll = 
{react: :thumbsup:}
{react: :thumbsdown:}
```

Embed:
```ruby
embed = 
{embed:
    {title: My Title}
    {description: My Description}
    {field[1]: Example:|This is an inline field}
    {field[0]: This field|is NOT inline}
    {color: 000000}
    
    {message.react: :ok_hand:}
}
```