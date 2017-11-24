package com.squadup.squadup.activity

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.squadup.squadup.R
import org.w3c.dom.Text

class GroupViewActivity : BaseActivity() {

    //setup the recycler view, layout manager, adapter needed to display the pizzas
    lateinit var RecyclerViewMembers: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var memberAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_group_view)

        //establish the name of the group
        var TextView = findViewById<TextView>(R.id.textViewGroupName)
        TextView.text = app.group!!.name

        //set up recycler view for members of group
        RecyclerViewMembers = findViewById(R.id.RecyclerViewGroupMembers)
        layoutManager = LinearLayoutManager(this) //set context for linear layout manager
        memberAdapter = MemberAdapter(ArrayList(app.group!!.members))
        RecyclerViewMembers.adapter = memberAdapter
        RecyclerViewMembers.layoutManager = layoutManager
        RecyclerViewMembers.adapter.notifyDataSetChanged()

    }

    fun meetUp(view: View){
        Handler().postDelayed({
            //TODO: Change this screen to the meet-up screen once master has been updated.
            presentScreen(MessagingTestActivity::class.java)
        }, 0)
    }


    class MemberAdapter(members: ArrayList<String>) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {
        var memberList: ArrayList<String>

        init {
            memberList = members
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.member_list, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var member = memberList.get(position)
            holder.memberName.text = member

        }

        override fun getItemCount(): Int {
            return memberList.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var memberName: TextView

            init {
                memberName = itemView.findViewById(R.id.textViewMemberName)
            }
        }

    }
}
