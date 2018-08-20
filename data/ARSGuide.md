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

[react]
Reacts to the users message.
Possible arguments:
- emote

[delete]
Deletes the original message.
No arguments.

[embed]
Sends an embed.
Possible arguments:
- &field
- &title
- &description
- &color
- &message.react

```

### SubFunctions
```ini
[message.react]
See &react.

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

### Variables
There are some variables you can use.
```ruby
%.user                  Username of ARS user.
%.channel               Channel name.
%.server                Server name.

%.userNickname          User's nickname. If they don't have one,
                        their username is used.
%.channelDescription    The channel description.
%.topRole               Name of the user's highest role.
%.userColor             Color of the current user.
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