发送端 -> 接收端<br/>
发送文件信息，请求接收端是否接收
```json
{
    "type": 1,
    "data": {
        "flieName": "文件名称",
        "fileSize": 111111,
        "fileHash": "文件hash",
        "commandPort": 111
    }
}
```
接收端 -> 发送端<br/>
发送是否接收文件
```json
{
    "type": 2,
    "data": {
        "isAccept": true,
        "sendPort": 1111
    }
}
```
发送端 -> 接收端<br/>
发送端要继续发送文件，请求接收端告诉发送端文件的接收情况
```json
{
    "type": 3,
    "data": {
        "fileHash": "文件hash"
    }
}
```
接收端 -> 发送端<br/>
发送文件接收情况
```json
{
    "type": 4,
    "data": {
        "fileHash": "文件hash",
        "sections": [
            {
                "startIndex": 111,
                "endIndex": 222,
                "finishIndex": 333
            },
            {
                "startIndex": 111,
                "endIndex": 222,
                "finishIndex": 333
            }
        ]
    }
}
```

发送端 -> 接收端<br/>
真正发送文件数据的时候，每个分块socket先发送以下信息
```json
{
    "fileHash": "文件hash",
    "startIndex": 111,
    "endIndex": 222,
    "finishIndex": 333
}
```