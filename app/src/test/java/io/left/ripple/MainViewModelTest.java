package io.left.ripple;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.util.RightMeshException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MainViewModelTest {
    //Executes each task synchronously using Architecture Components.
    //Using for testing Android ViewModel
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    private Application application;
    @Mock
    private RightMeshConnector rightMeshConnector;
    @Mock
    private MeshId mockMeshId;

    private MainViewModel spyViewModel;

    /**
     * Run before each test method.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        MainViewModel underTest = new MainViewModel(application);
        underTest.setRightMeshConnector(rightMeshConnector);
        spyViewModel = Mockito.spy(underTest);
    }

    @Test
    public void init_isCalled() {
        //Trigger
        spyViewModel.init();

        //verify
        verify(rightMeshConnector).setOnConnectSuccessListener(any());
        verify(rightMeshConnector).setOnPeerChangedListener(any());
        verify(rightMeshConnector).setOnDataReceiveListener(any());
        verify(spyViewModel).init();
    }

    @Test
    public void toRightMeshWalletActivty_isCalled() throws RightMeshException {
        //Trigger
        spyViewModel.toRightMeshWalletActivty();

        //verify
        verify(rightMeshConnector).toRightMeshWalletActivty();
        verify(spyViewModel).toRightMeshWalletActivty();
    }

    @Test
    public void sendColorMsg_nullTargetMeshId() throws RightMeshException {
        MeshId targetId = null;
        Colour msgColor = Colour.RED;
        String payload = String.valueOf(targetId) + ":" + msgColor;

        //Trigger
        spyViewModel.sendColorMsg(targetId, msgColor);

        //verify
        verify(rightMeshConnector, never()).sentDataReliable(targetId, payload);
        verify(spyViewModel).sendColorMsg(targetId, msgColor);
    }

    @Test
    public void sendColorMsg_targetMeshId() throws RightMeshException {
        MeshId targetId = mockMeshId;
        Colour msgColor = Colour.RED;
        String payload = String.valueOf(targetId) + ":" + msgColor;

        //Trigger
        spyViewModel.sendColorMsg(targetId, msgColor);

        //verify
        verify(rightMeshConnector).sentDataReliable(targetId, payload);
        verify(spyViewModel).sendColorMsg(targetId, msgColor);
    }

    @Test
    public void onCleared_isCalled() throws RightMeshException {
        //Trigger
        spyViewModel.onCleared();

        //verify
        verify(rightMeshConnector).stop();
        verify(spyViewModel).onCleared();
    }
}
