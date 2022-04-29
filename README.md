# 典型的领域驱动设计应用架构

本篇主要是对典型的领域驱动设计 (Domain Driven Design) 应用架构的理解和使用经验的总结。

## 领域模型的项目结构

一个基于 DDD 设计模式的 SpringBoot [项目结构](https://github.com/damonYuan/ddd_example) 通常划分如下

```
.
├── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── damonyuan
    │   │           └── ddd
    │   │               ├── config
    │   │               ├── controller
    │   │               ├── dao
    │   │               ├── domain
    │   │               ├── service
    │   │               │   ├── dto
    │   │               │   └── impl
    │   │               └── utils
    │   └── resources
    │       ├── static
    │       └── templates
    └── test
        └── java
            └── com
                └── damonyuan
                    └── ddd
```

其中

- config: 所有 SpringBoot 的配置文件都放置在这里
- controller: 所有项目的 API 入口 controller 文件
- dao: Data Access Object，在一些项目 (如 Hibernate) 中也可以叫 repository。这里的类负责处理向数据库，其他 API 或者文件系统增删改查数据
- domain: 从数据库的角度可以比较好理解，一个 domain 类一般代表数据库中的一张表，有着数据库表结构中相对应的字段
- service: 该目录中存储了所有 Service 的 Interface。服务是业务逻辑处理的地方，通过 Interface 对业务逻辑的定义和实现进行解耦是进行单元测试的前提
- service/dto: 有的时候 controller 定义的 API 返回的值不一定就恰好是 domain 代表的数据，它可能是多个 domain 的集合。这种情况就需要有一个 Data Transfer Object 对数据进行集合和变换以满足前端需求
- service/impl: 该目录下的类为 Service interfaces 的实现
- utils: 该目录下为工具类，一般只应该包含静态方法

## 领域模型的 4 种模式

[领域设计模式（Domain Driven Design）](https://martinfowler.com/tags/domain%20driven%20design.html) 是由 Martin Fowler 提出的一种设计模式，其模型一般可以分为4大类：

1. 失血模式: 简单来说，就是 Domain Object 只有属性的getter/setter方法的纯数据类，所有的业务逻辑完全由 Service 完成
2. 贫血模式 ([Anemia Domain Model](https://martinfowler.com/bliki/AnemicDomainModel.html)) : 简单来说，就是 Domain Object 包含了不依赖于持久化的领域逻辑，而那些依赖持久化的领域逻辑 (与 DAO 层打交道的逻辑) 被分离到 Service 层
3. 充血模式: 充血模型和第二种模型差不多，所不同的就是如何划分业务逻辑，即认为，绝大多业务逻辑都应该被放在 Domain Object 里面(包括持久化逻辑)，而 Service 层应该是很薄的一层，仅仅封装事务和少量逻辑，不和 DAO 层打交道
4. 胀血模式: 取消 Service 层，只剩下 Domain Object 和 DAO 两层，在 Domain Object 上面封装事务

### 失血模式

这种模式下 Domain Object 就只是数据库表在 POJO (Plain Old Java Object) 上的一个映射。

  ```
  Controller -> Service (domain logic) -> DAO -> Domain
                    |                              ^
                    └------------------------------|
  ```

优点:

- 简单。所有业务逻辑都在 Service 层实现，Service 通过调用 DAO 中的方法对 Domain 所代表的数据进行存取

缺点:

- anti-OOP。按照 Object-Oriented Programming 的理念，一个 Object 应该包括它所代表的事物的方法。比如 `Cat` 类所生成的一个实例 `cat` 应该有 `meow()` 这个方法，而不应该把 `meow()` 这个方法放到 `CatService` 里去

### 贫血模式 ([Anemia Domain Model](https://martinfowler.com/bliki/AnemicDomainModel.html))

这种模式和上面失血模式最重要的区别是 Domain Object 和 Service Implementation 中都包含了领域逻辑，其划分标准是

- 依赖于持久化（换一种说法就是需要通过 DAO 向数据库读写数据）的领域逻辑分离到 Service 层
- 不依赖于持久化的领域逻辑包含在 Domain 中

Martin Fowler一直主张该模型。

优点:

- 各层单向依赖，结构清楚，易于实现和维护
  ```
  Controller -> Service (persistence-releted domain logic) -> DAO -> Domain (non-persistence-related domain logic)
                  |                                                    ^
                  └─---------------------------------------------------| 
  ```
- 设计简单易行，底层模型非常稳定

缺点:

- 比较紧密依赖的持久化 Domain Logic 被分离到 Service 层，显得不够 Object-Oriented
- Service 层过于厚重

### 充血模式

充血模型和第二种模型差不多，所不同的就是如何划分业务逻辑，即认为，绝大多业务逻辑都应该被放在 Domain Object 里面(包括持久化逻辑)，而 Service 层应该是很薄的一层，仅仅封装事务和少量逻辑，不和DAO层打交道。

  ```
  Controller -> Service -> Domain (domain logic) <--> DAO
  ```

优点:

- 更加符合 OO 的原则
- Service 层很薄，只充当 Facade 的角色，不和 DAO 打交道

缺点:

- DAO 和 Domain Object 形成了双向依赖，复杂的双向依赖会导致很多潜在的问题
- 如何划分 Service 层逻辑和 Domain 层逻辑是非常含混的，在实际项目中，由于设计和开发人员的水平差异，可能导致整个结构的混乱无序
- 考虑到 Service 层的事务封装特性，Service 层必须对所有的 Domain Object 的逻辑提供相应的事务封装方法，其结果就是 Service 完全重定义一遍所有的 domain logic，非常烦琐，使得和贫血模型没有什么区别了

### 胀血模式

取消 Service 层，只剩下 Domain Object 和 DAO 两层，在 Domain Object 上面封装事务。

  ```
  Controller -> Domain (domain logic) <--> DAO
  ```

Ruby on Rails 就是这种模式，甚至 Domain 和 DAO 也直接合并了，但是这一部分是由 Ruby 这门语言决定的。 Ruby 的动态 metaprogramming 特性和 module 之间的继承关系在编译器的层面上实现了逻辑分层， AOP 和 Dependency Injection，从而使得 Unit Test 中所依赖的 mock & stub 手段不需要通过设计模式中的分层和 Java 中的 interface 来实现。

在 Java 项目中，其优点是:

- 简化了分层
- 比较符合 OO

缺点:

- 很多不是 domain logic 的 Service 逻辑也被强行放入 Domain Object ，引起了 Domain Object 模型的不稳定
- Domain Object 暴露给 controller 层过多的信息，可能引起意想不到的副作用

## Controller vs Service

什么应该放在 Controller 里呢，一般标准如下：

1. API endpoints exposure 应该放在 Controller 里
2. 安全相关逻辑，比如 param filter, user authentication & authorization 应放在 Controller 里
3. Request 中的收到数据的校验应该放在 Controller 里
4. 下一步 Controller 应该调用 Service 方法并传入参数，获取 dto response 回复给前端

## 经验总结

在这四种模型当中，失血模型和胀血模型应该是不被提倡的。而贫血模型和充血模型从技术上来说，都已经是可行的了。但是我个人仍然主张使用贫血模型。其理由：

1. 参考充血模型第三个缺点，Service 层只是 Domain 层的一个映射，已经没有太大意义了，反而增加了工作量（在 Domain 中定义好了的一个 domain logic 在 Service 层需要再包装一遍）
2. 参考充血模型第三个缺点，不同的 Services 的逻辑集中在一个 Domain 中，必然使得 Domain 及其厚重
3. domain object和DAO的双向依赖在做大项目中，考虑到团队成员的水平差异，很容易引入不可预知的潜在bug
4. 如何划分 domain logic 和 service logic 的标准是不确定的，往往要根据个人经验，有些人就是觉得某个业务他更加贴近 domain，也有人认为这个业务是贴近 service 的。由于划分标准的不确定性，带来的后果就是实际项目中会产生很多这样的争议和纠纷，不同的人会有不同的划分方法，最后就会造成整个项目的逻辑分层混乱。这不像贫血模型中我提出的按照是否依赖持久化进行划分，这种标准是非常确定的，不会引起争议，因此团队开发中，不会产生此类问题
5. 贫血模型的 domain object 确实不够 rich，但是我们是做项目，不是做研究，好用就行了，管它是不是那么纯的 OO 呢？
