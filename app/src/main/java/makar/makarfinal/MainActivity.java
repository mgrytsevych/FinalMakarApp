package makar.makarfinal;

import android.content.Intent;
import android.text.format.DateFormat;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;



public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;
    FloatingActionButton fab;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override

                public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(activity_main,"Succesfully sign out.",Snackbar.LENGTH_SHORT).show();

                    // Exiting the app with a snackbar message
                finish();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST) {
            if(resultCode == RESULT_OK) {
                //Welcome snackbar
                Snackbar.make(activity_main,"Welcome to our chat.", Snackbar.LENGTH_SHORT).show();
                displayChatMessage();
            } else {
                // error message snackbar
                Snackbar.make(activity_main,"Sorry, error occured. Try again later!", Snackbar.LENGTH_SHORT).show();

                // Exiting the app
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main = (RelativeLayout) findViewById(R.id.activity_main);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reads the user message field and adds a new entry of ChatMessage to the Firebase

                EditText input = (EditText) findViewById(R.id.input);
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(input.getText().toString(),
                        FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                // Clears the message field after user clicked FAB to post the message and it was uploaded to Firebase
                input.setText("");

            }
        });

        //Check if user is signed if not redirect to Sign in page, starts sign-up/in activity


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST);
        } else {
            // if user is signed in creates a Welcome snackbar
            Snackbar.make(activity_main, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();

            //Load chat room contents
            displayChatMessage();
        }


    }

    private void displayChatMessage() {

        ListView listOfMessages = (ListView)findViewById(R.id.list_of_message);
        adapter = new FirebaseListAdapter<ChatMessage>(this,ChatMessage.class,R.layout.list_of_items,FirebaseDatabase.getInstance().getReference())
        {
           @Override
            protected void populateView(View v, ChatMessage model, int position) {
               //Reference to item list xml.
               TextView messageText,messageUser,messageTime;
               messageText = (TextView)v.findViewById(R.id.message_text);
               messageUser = (TextView)v.findViewById(R.id.message_user);
               messageTime = (TextView)v.findViewById(R.id.message_time);
               //getting the user imput and name
               messageText.setText(model.getMessageText());
               messageUser.setText(model.getMessageUser());
               //formats the date before displaying it
               messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",model.getMessageTime()));

           }
        };
        listOfMessages.setAdapter(adapter);
    }

}