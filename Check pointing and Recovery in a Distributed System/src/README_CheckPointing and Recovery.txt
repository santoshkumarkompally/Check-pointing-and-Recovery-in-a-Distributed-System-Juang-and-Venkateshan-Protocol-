- Changed the object used for communication. 
Created a new Object called Message which includes NodeInfo, no of sent messages(also helps to identify if it is an application message or a rollback message), and the nodeId of the sender node.

- If it is an application message, the Message object will have noOfSentMessage=-1.

- If it is a rollback message, the Message object will have noOfSentMessages = noOfMessages sent to the recipient node (obtained from checkpoint)

- Added a new entry in REB which says myFailureCheckpoints which contains the number of checkpoints after which the process fails, in order.

- Added a new entry to Checkpoint called indexSinceLastRollback. This is used to track when the process is supposed to fail, ie after how many checkpoints. This value is set to 0 every time a process fails. If this happens, remove the first entry from myFailureCheckpoints. Now the count (indexSinceLastRollback) is reset and new failure point is targeted.

-For a rollback message, all that is needed is the number of sent messages of this node to each of its neighbours. 

-Once a node received a rollback message, it keeps rolling back till the number of received messages=number of sent messages from sender node. Or, till the last checkpoint is reached (additional check - not needed if it works correctly). Then this node sends a rollback message to all its neighbours if it has rolled back. The promoteRollback variable checks if a rollback occurred or not.

ALERT!!!!
NOTE : Rollback messages are not counted towards maxNumber of messages at all. 
-Should verify what happens when passive processes get any rollback messages - if they do.

 
