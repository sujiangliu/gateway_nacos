# spring cloud gateway 使用 nacos 注册中心，实现动态路由的demo

nacos配置中心的配置格式：json
[
    {
        "id": "user_route",
        "uri": "lb://user-service",
        "order": 0,
        "filters": [],
        "predicates": [
            {
                "args": {
                    "pattern": "/user/**"
                },
                "name": "Path"
            }
        ]
    },
    {
        "id": "message_route1",
        "uri": "lb://message-service",
        "order": 0,
        "filters": [
            {
                "name": "StripPrefix",
                "args": {
                    "parts": 1,
                    "order": 2
                }
            }
        ],
        "predicates": [
            {
                "args": {
                    "pattern": "/message/**"
                },
                "name": "Path"
            }
        ]
    }
]
