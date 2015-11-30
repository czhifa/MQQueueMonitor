package org.erc.qmm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.erc.qmm.config.QueueConfig;
import org.erc.qmm.i18n.Messages;
import org.erc.qmm.monitor.PollEvent;
import org.erc.qmm.monitor.PollListener;
import org.erc.qmm.monitor.QueueMonitor;
import org.erc.qmm.mq.JMQQueue;

/**
 * The Class MonitorPanel.
 */
public class MonitorPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1181783867179893568L;

	/** The total enqueued. */
	private long totalEnqueued = 0;
	
	/** The total dequeued. */
	private long totalDequeued = 0;

	/** The alert label. */
	private JLabel alertLabel;
	
	/** The items input label. */
	private JLabel itemsInputLabel;
	
	/** The items output label. */
	private JLabel itemsOutputLabel;
	
	/** The chart. */
	private GraphPanel chart;
	
	/** The monitor. */
	private QueueMonitor monitor;

	/** The start time. */
	private long startTime=0;
	
	/** The input per second. */
	private long inputPerSecond = 0;
	
	/** The output per second. */
	private long outputPerSecond = 0;
	
	/** The max out per second. */
	private long maxOutPerSecond =0;
	
	/** The max in per second. */
	private long maxInPerSecond = 0;
	
	/** The last time. */
	private long lastTime = 0;
	
	/**
	 * Create the panel.
	 */
	public MonitorPanel() {
		chart = new GraphPanel();
		setLayout(new BorderLayout());
		add(chart);
		
		JPanel alertPanel = new JPanel(new FlowLayout());
		alertLabel = new JLabel(new ImageIcon(getClass().getResource(Images.OK))); //$NON-NLS-1$
		alertLabel.setText(Messages.getString("MonitorPanel.alert_ok")); //$NON-NLS-1$
		alertPanel.add(alertLabel);
		
		itemsInputLabel = new JLabel(new ImageIcon( getClass().getResource("/org/erc/qmm/img/small/receive.png"))); //$NON-NLS-1$
		itemsInputLabel.setText(MessageFormat.format(Messages.getString("MonitorPanel.totalinfoInput"), 0,0,0)); //$NON-NLS-1$
		alertPanel.add(itemsInputLabel);
		
		itemsOutputLabel = new JLabel(new ImageIcon( getClass().getResource("/org/erc/qmm/img/small/send.png"))); //$NON-NLS-1$
		itemsOutputLabel.setText(MessageFormat.format(Messages.getString("MonitorPanel.totalinfoOutput"), 0,0,0)); //$NON-NLS-1$
		alertPanel.add(itemsOutputLabel);
        add(alertPanel, BorderLayout.SOUTH);
        
	}

	/**
	 * Adds the.
	 *
	 * @param depth the depth
	 * @param processed the processed
	 * @param enqueued the enqueued
	 */
	private void add(int depth, int processed, int enqueued){
		totalEnqueued += enqueued;
		totalDequeued += processed;
		if(lastTime <1){
			lastTime = startTime;
		}
		long time = (System.currentTimeMillis() - lastTime) / 1000;
		lastTime = System.currentTimeMillis();
		
		if(time>0){
			inputPerSecond  = (enqueued / time);
			outputPerSecond = (processed / time);
		
			if(inputPerSecond>maxInPerSecond){
				maxInPerSecond = inputPerSecond;
			}
			if(outputPerSecond>maxOutPerSecond){
				maxOutPerSecond = outputPerSecond;
			}
			itemsInputLabel.setText(MessageFormat.format(Messages.getString("MonitorPanel.totalinfoInput"),totalEnqueued,inputPerSecond,maxInPerSecond)); //$NON-NLS-1$
			itemsOutputLabel.setText(MessageFormat.format(Messages.getString("MonitorPanel.totalinfoOutput"),totalDequeued,outputPerSecond,maxOutPerSecond)); //$NON-NLS-1$
			chart.addScore(enqueued,processed,depth);
		}
	}
	
	/**
	 * Sets the alarm.
	 *
	 * @param on the new alarm
	 */
	private void setAlarm(boolean on){
		if (on){
			alertLabel.setIcon(new ImageIcon(getClass().getResource(Images.ALERT))); //$NON-NLS-1$
			alertLabel.setText(Messages.getString("MonitorPanel.alert_bad")); //$NON-NLS-1$
		}else{
			alertLabel.setIcon(new ImageIcon(getClass().getResource(Images.OK))); //$NON-NLS-1$
			alertLabel.setText(Messages.getString("MonitorPanel.alert_ok")); //$NON-NLS-1$
		}
	}

	/**
	 * Load with.
	 *
	 * @param queue the queue
	 * @throws Exception 
	 */
	public void loadWith(QueueConfig queue) throws Exception{		
		startTime = System.currentTimeMillis();
		monitor = new QueueMonitor(queue);
		monitor.addPollListener(new PollListener() {
			@Override
			public void action(PollEvent e) {
				add(e.getDepth(),e.getDequeued(), e.getEnqueued());
				if (e.getMaxDepth() * 0.9 <e.getDepth()){
					setAlarm(true);
				}else{
					setAlarm(false);
				}
			}
		});
		monitor.start();
	}
	
	/**
	 * Gets the queue.
	 *
	 * @return the queue
	 */
	public JMQQueue getQueue(){
		return monitor.getQueue();
	}
}