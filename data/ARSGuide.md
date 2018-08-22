
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
```ruby
!sendme = 
{dm: Some text}
{user: 518753928735
    {dm: %.user used sendme}
}
```

![Alt text](https://g.gravizo.com/source/marker_10?https%3A%2F%2Fraw.githubusercontent.com%2FAllTheMods%2FAllTheBots%2Fmaster%2Fdata%2FARSGuide.md)

<details> 
<summary></summary>
marker_10
digraph Example {
  graph[pad="0.5", nodesep="1", ranksep="2"]
  rankdir="LR"

  Init[shape=Msquare]
  Init -> function_message
  Init -> function_dm
  Init -> function_react
  Init -> function_delete
  Init -> function_embed
  Init -> function_role_add
  Init -> function_role_remove
  Init -> function_pin
  Init -> get_member
  Init -> get_role
  Init -> get_channel
  Init -> conditional_if

  subgraph cluster_0 {
    label = "Functions"
    function_message
    function_dm
    function_react
    function_delete
    function_embed
    function_role_add
    function_role_remove
    function_pin
    function_edit
  }

  subgraph cluster_1 {
  label = "getters"
  get_member
  get_role
  get_channel
  }

  subgraph cluster_2 {
  label = "Variable Types"
  Member
  Channel
  Message
  Role
  Boolean
  }

  subgraph cluster_3 {
  label = "Conditions"
  conditional_if
  }

  get_member -> Member
  get_role -> Role
  get_channel -> Channel

  Member -> function_message
  Member -> function_dm
  Member -> function_role_add
  Member -> function_role_remove
  Channel -> function_message
  Channel -> function_embed
  Message -> function_react
  Message -> function_delete
  Message -> function_edit
  Message -> function_pin

  function_message -> Message
  function_embed -> Message
  function_dm -> Message

  conditional_if -> Init
}
marker_10
</details>