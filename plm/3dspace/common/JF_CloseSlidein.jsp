<%--解决command直接href 中放置table表格 导致无法正常关闭--%>
<script>
    const  tableWin = findFrame(getTopWindow(),"detailsDisplay");
    if (tableWin){
        tableWin.emxEditableTable.refreshStructure()
    }
    // debugger;
    // parent.window.closeWindow()
</script>