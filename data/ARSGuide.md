
# ARS GUIDE  
  
Format:   
```ruby  
<triggername> = {<function>: <arguments>}  
```  
There can be an infinite amount of functions per trigger.  
Trigger names have to be unique.  
  
*note: `&...` means it's a function*   

---

### Functions:  
```ini  
[message]  
Sends a message.  
Passes through: created message
Possible arguments:  
- text  
- &react  
- &delete
- &edit
- &pin
  
[dm]  
Sends the user a DM
Passes through: created message
Possible arguments:
- text  
- &react  
- &delete
- &edit
- &pin

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
Passes through: created message with embed
Possible arguments:  
- &field  
- &title  
- &description  
- &color  
- &react  
- &delete  
- &edit
- &pin
  
[role.add]  
Gives a role to the user.  
Possible arguments:  
- role name  
  
[role.remove]  
Removes a role from the user.  
Possible arguments:  
- role name

[pin]
Pins a message.
No arguments.
```  
---
  
### SubFunctions  
These should never be used on top level.
```ini  
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

[edit]
Edits a message.
Possible arguments:
- text
- time (in seconds)
```  
---
  
### Conditional  
Some functions are conditionals or checkers. These are usually top-level, but may appear as subfunctions.  
```ini  
[if]  
Possible arguments:  
- any &condition  
- functions
- "elseif" any &condition
- functions for elseif
- "else"  
- functions for else 
  
[message.contains]
Checks if the message contains a certain string.
Can be inverted by prepending `!`
Possible arguments:  
- text

[nickname.contains]
Checks if the nickname contains a certain string.
Can be inverted by prepending `!`
Possible arguments:
- text

[parameter]
Checks if the passed parameter is equal to a certain string.
Can be inverted by prepending `!`
Possible arguments:
- text
```  
---

### Variables  
There are some variables you can use.  
```ruby  
%.user                  Username of ARS user.  
%.channel               Channel name.  
%.server                Server name.  
  
%.userNickname          User's nickname, if they don't have one,  
                        their username is used.
%.userColor             Color of the current user.  
%.userMention           Mentions the user.  
%.channelDescription    The channel description.  
%.channelMention        Mentions the channel.  
%.topRole               Name of the user's highest role.  
```
---

### Pulling variables
Some variables can be pulled by getters:
```ini
[user]
Gets a user based on ID.
Passes through: User
Possible arguments:
- ID

[channel]
Gets a channel based on ID.
Passes through: Channel
Possible arguments:
- ID

[role]
Gets a role based on ID.
Passes through: Role
Possible arguments:
- ID
```
---

### Examples:  
Poll:  
```ruby  
!poll = 
{react: :thumbsup:}  
{react: :thumbsdown:}  
```  
  
Embed:  
```ruby  
!sampleEmbed = 
{embed:  
    {title: My Title}
    {description: My Description}
    {field[1]: Example:|This is an inline field}
    {field[0]: This field|is NOT inline}
    {color: 000000}

    {message.react: :ok_hand:}  
}
```

Dm something to the user, and notify a specific person:
```
!sendme = 
{dm: Some text}
{user: 518753928735
    {dm: %.user used sendme}
}
```