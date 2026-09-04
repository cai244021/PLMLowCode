1.该Widget为创建物理产品
2.修改地方在发送请求获取物理产品模版时将V_Name和Filename中的物理产品改为英文
3.P3696为新增替换函数replacePhysicalProduct
4.P3717为新增函数调用
5.bak文件夹下为源js
6.新增 getCurrentUserHasStandardRoleAndHandle 函数 获取需要移除的属性中range值
7.修改 parseTypeInfo 为异步 async 函数 同步请求
8.零件分类和零件子类型做层了级联
9.“分类”名称改为“零件名称库/书签”
10.新增创建零件和产品时校验分类不能为空 P316 checkClassIsNotNull 该函数主要获取库信息Tab页中选中分类物理id
11.新增只有XEN才走修改逻辑，其他APP不走
2025/0723 陈彦修改逻辑
新增发布事件JFUpdatePartType选库后调用更新零件类型和详情分类
新增 将标题 mandatory 设置为false 将零件中文、零件英文、详细类别-中文、详细类别-英文 mandatory设置为true
新增 将必填属性按照固定顺序显示