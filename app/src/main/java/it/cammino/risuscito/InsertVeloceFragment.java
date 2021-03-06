package it.cammino.risuscito;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.cammino.risuscito.adapters.CantoInsertRecyclerAdapter;
import it.cammino.risuscito.objects.CantoInsert;

public class InsertVeloceFragment extends Fragment {

    // create boolean for fetching data

    private DatabaseCanti listaCanti;
    private List<CantoInsert> titoli;
//    private EditText searchPar;
    private View rootView;
//    RecyclerView mRecyclerView;
    CantoInsertRecyclerAdapter cantoAdapter;

    private int fromAdd;
    private int idLista;
    private int listPosition;

    private LUtils mLUtils;

    private long mLastClickTime = 0;

    @BindView(R.id.matchedList) RecyclerView mRecyclerView;
    @BindView(R.id.search_no_results) View mNoResults;
    @BindView(R.id.textfieldRicerca) EditText searchPar;

    @OnClick(R.id.pulisci_ripple)
    public void pulisciRisultati() {
        searchPar.setText("");
        mNoResults.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_ricerca_titolo, container, false);
        ButterKnife.bind(this, rootView);

//        searchPar = (EditText) rootView.findViewById(R.id.textfieldRicerca);
        listaCanti = new DatabaseCanti(getActivity());

//        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.matchedList);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                SQLiteDatabase db = listaCanti.getReadableDatabase();

                String idCanto = ((TextView) v.findViewById(R.id.text_id_canto))
                        .getText().toString();

                if (fromAdd == 1)  {
                    // chiamato da una lista predefinita
                    String query = "INSERT INTO CUST_LISTS ";
                    query+= "VALUES (" + idLista + ", "
                            + listPosition + ", "
                            + idCanto
                            + ", CURRENT_TIMESTAMP)";
                    try {
                        db.execSQL(query);
                    } catch (SQLException e) {
                        Snackbar.make(rootView, R.string.present_yet, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
                else {
                    //chiamato da una lista personalizzata
                    String query = "SELECT lista" +
                            "  FROM LISTE_PERS" +
                            "  WHERE _id =  " + idLista;
                    Cursor cursor = db.rawQuery(query, null);
                    // recupera l'oggetto lista personalizzata
                    cursor.moveToFirst();

                    ListaPersonalizzata listaPersonalizzata = (ListaPersonalizzata) ListaPersonalizzata.
                            deserializeObject(cursor.getBlob(0));

                    // chiude il cursore
                    cursor.close();

                    listaPersonalizzata.addCanto(String.valueOf(idCanto), listPosition);
                    cursor.close();

                    ContentValues  values = new  ContentValues( );
                    values.put("lista" , ListaPersonalizzata.serializeObject(listaPersonalizzata));
                    db.update("LISTE_PERS", values, "_id = " + idLista, null );
                    db.close();
                }

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
                getActivity().overridePendingTransition(0, R.anim.slide_out_right);
            }
        };

        View.OnClickListener seeOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < Utility.CLICK_DELAY)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                // recupera il titolo della voce cliccata
                String idCanto = ((TextView) v.findViewById(R.id.text_id_canto))
                        .getText().toString();
                String source = ((TextView) v.findViewById(R.id.text_source_canto))
                        .getText().toString();

                // crea un bundle e ci mette il parametro "pagina", contente il nome del file della pagina da visualizzare
                Bundle bundle = new Bundle();
                bundle.putString("pagina", source);
                bundle.putInt("idCanto", Integer.parseInt(idCanto));

                // lancia l'activity che visualizza il canto passando il parametro creato
                startSubActivity(bundle, v);
            }
        };

        // Creating new adapter object
        titoli = new ArrayList<>();
        cantoAdapter = new CantoInsertRecyclerAdapter(getActivity(), titoli, clickListener, seeOnClickListener);
        mRecyclerView.setAdapter(cantoAdapter);

        // Setting the layoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Bundle bundle = getArguments();
        fromAdd = bundle.getInt("fromAdd");
        idLista = bundle.getInt("idLista");
        listPosition = bundle.getInt("position");

        searchPar.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String tempText = ((EditText) getActivity().findViewById(R.id.tempTextField)).getText().toString();
                if (!tempText.equals(s.toString()))
                    ((EditText) getActivity().findViewById(R.id.tempTextField)).setText(s);

                if (s.length() >= 3) {

//                    rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
                    mNoResults.setVisibility(View.GONE);

//                    String titolo = Utility.duplicaApostrofi(s.toString());
                    String stringa = Utility.removeAccents(s.toString()).toLowerCase();
                    String titoloTemp;
                    Log.d(getClass().getName(), "onTextChanged: stringa " + stringa);

                    // crea un manipolatore per il Database in modalità READ
                    SQLiteDatabase db = listaCanti.getReadableDatabase();

                    // lancia la ricerca di tutti i titoli presenti in DB e li
                    // dispone in ordine alfabetico
                    String query = "SELECT titolo, color, pagina, _id, source"
//                            + "		FROM ELENCO" + "		WHERE titolo like '%"
//                            + titolo + "%'" + "		ORDER BY titolo ASC";
                            + "		FROM ELENCO ORDER BY titolo ASC";
                    Cursor lista = db.rawQuery(query, null);

                    // recupera il numero di record trovati
                    int total = lista.getCount();

                    // crea un array e ci memorizza i titoli estratti
                    titoli.clear();
                    lista.moveToFirst();
                    for (int i = 0; i < total; i++) {
                        titoloTemp = Utility.removeAccents(lista.getString(0).toLowerCase());
                        if (titoloTemp.contains(stringa))
                            titoli.add(new CantoInsert(Utility.intToString(lista.getInt(2), 3)
                                    + lista.getString(1) + lista.getString(0)
                                    , lista.getInt(3)
                                    , lista.getString(4)));
                        lista.moveToNext();
                    }

                    // chiude il cursore
                    lista.close();
                    cantoAdapter.notifyDataSetChanged();


                    if (total == 0)
//                        rootView.findViewById(R.id.search_no_results).setVisibility(View.VISIBLE);
                        mNoResults.setVisibility(View.VISIBLE);
                }
                else {
                    if (s.length() == 0) {
                        titoli.clear();
                        cantoAdapter.notifyDataSetChanged();
//                        rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
                        mNoResults.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

        });

        searchPar.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //to hide soft keyboard
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        ((EditText) getActivity().findViewById(R.id.tempTextField)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String tempText = searchPar.getText().toString();
                if (!tempText.equals(s.toString()))
                    searchPar.setText(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) { }

        });

//        rootView.findViewById(R.id.pulisci_ripple).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchPar.setText("");
////                rootView.findViewById(R.id.search_no_results).setVisibility(View.GONE);
//                mNoResults.setVisibility(View.GONE);
//            }
//        });

        mLUtils = LUtils.getInstance(getActivity());

        return rootView;
    }

    /**
     * Set a hint to the system about whether this fragment's UI is currently visible
     * to the user. This hint defaults to true and is persistent across fragment instance
     * state save and restore.
     * <p/>
     * <p>An app may set this to false to indicate that the fragment's UI is
     * scrolled out of visibility or is otherwise not directly visible to the user.
     * This may be used by the system to prioritize operations such as fragment lifecycle updates
     * or loader ordering behavior.</p>
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d(getClass().getName(), "VISIBLE");
            //to hide soft keyboard
            if (searchPar != null)
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(searchPar.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        if (listaCanti != null)
            listaCanti.close();
        super.onDestroy();
    }

    private void startSubActivity(Bundle bundle, View view) {
        Intent intent = new Intent(getActivity().getApplicationContext(),
                PaginaRenderActivity.class);
        intent.putExtras(bundle);
        mLUtils.startActivityWithTransition(intent, view, Utility.TRANS_PAGINA_RENDER);
    }

}
