package com.github.rkbalgi.intellij.plugins.iso8583parser

import com.github.rkbalgi.iso4k.IsoField
import com.github.rkbalgi.iso4k.Message
import com.github.rkbalgi.iso4k.Spec
import com.github.rkbalgi.iso4k.fromHexString
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.TextField
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath


class ISOParser : AnAction("") {
    override fun actionPerformed(e: AnActionEvent) {
        var panel = ToolWindowManager.getInstance(e.project!!)
            .getToolWindow("ISO8583")!!.contentManager.getContent(0)!!.component as ISO8583Panel

        println(e.inputEvent.component)
        println(e.inputEvent.component.parent)


        val editor = e.getRequiredData(CommonDataKeys.EDITOR);
        val doc = editor.document

        panel.update(e.project!!, editor.caretModel.primaryCaret.selectedText ?: "")
        //update(e);

    }

    override fun update(e: AnActionEvent) {
        //super.update(e)
    }
}


class ISO8583ToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        val myToolWindow = ISO8583ToolWindow(toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(myToolWindow.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }

}

class ISO8583Panel : JBTabbedPane() {

    private val p = JPanel()
    private var spec: Spec? = Spec.spec("SampleSpec")

    init {

        p.layout = BorderLayout()
        add("Parsed ISO", p)
    }

    fun update(project: Project, hexData: String) {


        var ba: ByteArray
        try {
            ba = fromHexString(hexData)
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationGroupManager.getInstance().getNotificationGroup("ISO8583-ng")
                .createNotification("Invalid ISO hexdump", NotificationType.ERROR).notify(project);
            return

        }

        var msgName = this.spec!!.findMessage(ba)
        if (msgName == null) {
            NotificationGroupManager.getInstance().getNotificationGroup("ISO8583-ng")
                .createNotification("No available ISO message for selected dump", NotificationType.ERROR)
                .notify(project);
            return
        }
        val specMsg = spec!!.message(msgName!!)!!
        var msg = specMsg.parse(ba)

        var rootNode = DefaultMutableTreeNode("ISO Spec");
        specMsg.fields().forEach {
            addToModel(it, msg, rootNode)
        }


        p.add(Tree(rootNode), BorderLayout.CENTER);

        validate()
        repaint()
    }

}


fun addToModel(field: IsoField, msg: Message, node: DefaultMutableTreeNode) {

    if (msg.get(field.name) != null) {
        val fieldNode = DefaultMutableTreeNode("${field.name} -> [${msg.get(field.name)?.encodeToString()}]")
        node.add(fieldNode)

        if (field.hasChildren()) {
            field.children?.forEach { addToModel(it, msg, fieldNode) }
        }

    }


}

class ISO8583ToolWindow(toolWindow: ToolWindow) {
    private val refreshToolWindowButton: JButton? = null
    private val hideToolWindowButton: JButton? = null
    private val currentDate: JLabel? = null
    private val currentTime: JLabel? = null
    private val timeZone: JLabel? = null
    private val panel = ISO8583Panel()

    init {
        //hideToolWindowButton!!.addActionListener { e: ActionEvent? -> toolWindow.hide(null) }
        //refreshToolWindowButton!!.addActionListener { e: ActionEvent? -> currentDateTime() }
    }

    fun getContent(): ISO8583Panel {
        return panel
    }


}