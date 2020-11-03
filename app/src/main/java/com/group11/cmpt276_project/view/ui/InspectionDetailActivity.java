package com.group11.cmpt276_project.view.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group11.cmpt276_project.R;
import com.group11.cmpt276_project.databinding.ActivityInspectionDetailBinding;
import com.group11.cmpt276_project.service.model.InspectionReport;
import com.group11.cmpt276_project.service.model.Violation;
import com.group11.cmpt276_project.view.adapter.ViolationAdapter;
import com.group11.cmpt276_project.view.adapter.interfaces.IItemOnClick;
import com.group11.cmpt276_project.viewmodel.InspectionReportViewModel;
import com.group11.cmpt276_project.viewmodel.InspectionReportsViewModel;
import com.group11.cmpt276_project.viewmodel.RestaurantsViewModel;
import com.group11.cmpt276_project.viewmodel.ViolationsViewModel;
import com.group11.cmpt276_project.viewmodel.factory.InspectionReportViewModelFactory;

import java.util.ArrayList;
import java.util.List;
/**
 This is an activity for the InspectionDetail.
 It allow users to see list of violations using RecyclerView.
 **/

public class InspectionDetailActivity extends AppCompatActivity {

    private final static String INDEX = "index";
    private final static String PARENT = "parent";

    public static Intent startActivity(Context context, int index, int parent) {
        Intent intent = new Intent(context, InspectionDetailActivity.class);
        intent.putExtra(INDEX, index);
        intent.putExtra(PARENT, parent);

        return intent;
    }

    private int parent;
    private int index;

    private ViolationsViewModel violationsViewModel;
    private InspectionReportsViewModel inspectionReportsViewModel;
    private RestaurantsViewModel restaurantsViewModel;
    private InspectionReportViewModel inspectionReportViewModel;

    private  List<Violation> violationList;
    private RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bind();
        this.observeVisibility();
    }

    public void onBackClicked() {
        Intent intent = RestuarantDetailActivity.startActivity(this, this.parent);
        startActivity(intent);
    }

    private void bind() {
        Intent intent = getIntent();
        this.index = intent.getIntExtra(INDEX, -1);
        this.parent = intent.getIntExtra(PARENT, -1);


        this.violationsViewModel = ViolationsViewModel.getInstance();
        this.inspectionReportsViewModel = InspectionReportsViewModel.getInstance();
        this.restaurantsViewModel = RestaurantsViewModel.getInstance();

        String trackingNumber = this.restaurantsViewModel.getByIndex(parent).getTrackingNumber();

        InspectionReport inspectionReport = this.inspectionReportsViewModel.getByIndexAndTrackingNumbe(trackingNumber, index);

        InspectionReportViewModelFactory inspectionReportViewModelFactory = new InspectionReportViewModelFactory(inspectionReport.getViolLump().length);
        this.inspectionReportViewModel = new ViewModelProvider(this, inspectionReportViewModelFactory).get(InspectionReportViewModel.class);

        this.violationList = this.getViolationList(inspectionReport.getViolLump());

        ActivityInspectionDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_inspection_detail);
        binding.setReport(inspectionReport);
        binding.setActivity(this);

        this.recyclerView = findViewById(R.id.violation_list);
    }

    private void observeVisibility() {

        this.inspectionReportViewModel.getIsVisibleData().observe(this, (data) -> {
            ViolationAdapter violationAdapter = new ViolationAdapter(violationList, data, new ViolationItemOnClick());
            this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            this.recyclerView.setAdapter(violationAdapter);
        });
    }

    private List<Violation> getViolationList(int[] violationIds){
        List<Violation> violationList = new ArrayList<>();

        for(int id: violationIds) {
            violationList.add(this.violationsViewModel.get(String.valueOf(id)));
        }

        return violationList;
    }

    private class ViolationItemOnClick implements IItemOnClick {

        @Override
        public void onItemClick(int position) {
            inspectionReportViewModel.setIsVisible(position);
        }
    }
}