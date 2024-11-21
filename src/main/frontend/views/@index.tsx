import {ViewConfig} from '@vaadin/hilla-file-router/types.js';
import { VerticalLayout } from '@vaadin/react-components/VerticalLayout';
import { HorizontalLayout } from '@vaadin/react-components/HorizontalLayout';
import { Icon } from '@vaadin/react-components/Icon';
import {Grid, GridColumn, Select} from "@vaadin/react-components";
import '@vaadin/vaadin-lumo-styles/all-imports.js';
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer, AreaChart, Area, BarChart, Bar,
   Sector, SectorProps,
} from 'recharts';
import { useState } from 'react';

function adjustColorBrightness(color: string, amount: number): string {
  const hslRegex = /hsl\(\s*(\d+),\s*(\d+)%,\s*(\d+)%\)/;
  const match = color.match(hslRegex);
  if (match) {
    let [hue, saturation, lightness] = match.slice(1).map(Number);
    lightness = Math.min(100, lightness + amount);
    return `hsl(${hue}, ${saturation}%, ${lightness}%)`;
  }
  return color;
}

export const config: ViewConfig = {
  menu: { order: 0, icon: 'line-awesome/svg/chart-area-solid.svg' },
  title: 'Dashboard',
  loginRequired: true,
};

export default function DashboardView() {
  const selectItems = [
    { label: 'Daily', value: 'by-day' },
    { label: 'Weekly', value: 'by-week' },
    { label: 'Monthly', value: 'by-month' },
    { label: 'Yearly', value: 'by-year' },
  ];

  // State for time frame selection
  const [timeFrame, setTimeFrame] = useState('by-day');

  // Sample data for the chart
  const [data, setData] = useState([
    { time: '2023-10-01', callsPositive: 2, callsNegative: 22, callsNeutral: 3 },
    { time: '2023-10-02', callsPositive: 7, callsNegative: 24, callsNeutral: 7 },
    { time: '2023-10-03', callsPositive: 10, callsNegative: 18, callsNeutral: 8 },
    { time: '2023-10-04', callsPositive: 21, callsNegative: 15, callsNeutral: 11 },
    { time: '2023-10-05', callsPositive: 44, callsNegative: 1, callsNeutral: 3 },
    // Add more data points as needed
  ]);

  const averageSentimentDailyData = [
    { time: '2024-11-01', averageSentiment: 1 },
    { time: '2024-11-17', averageSentiment: 4 },
    { time: '2024-11-18', averageSentiment: 10 },
    { time: '2024-11-19', averageSentiment: 21 },
    { time: '2024-11-20', averageSentiment: 1 },
    { time: '2024-11-21', averageSentiment: 1 },
  ];

  const topTopicsData = [
    { topic: 'Philosophy', count: 23 },
    { topic: 'Chit chat', count: 11 }
  ];

  const xLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
  const yLabels = ['Billing', 'Support', 'Account', 'Sales', 'Cancellation'];
  const heatmapData = [
    [0.2, -0.5, 0.1, 0.3, -0.1],
    [-0.1, 0.4, -0.2, 0.5, 0.2],
    [0.3, -0.3, 0.6, -0.2, 0.1],
    [0.5, 0.2, -0.4, 0.2, -0.3],
    [-0.2, 0.1, 0.3, -0.1, 0.4],
  ];

  const heatmapDataForRecharts = [];

  yLabels.forEach((yLabel, yIndex) => {
    xLabels.forEach((xLabel, xIndex) => {
      heatmapDataForRecharts.push({
        x: xIndex,
        y: yIndex,
        xLabel: xLabel,
        yLabel: yLabel,
        value: heatmapData[yIndex][xIndex],
      });
    });
  });





  // Handle time frame change
  const handleTimeFrameChange = (event: CustomEvent) => {
    const newTimeFrame = event.detail.value;
    setTimeFrame(newTimeFrame);
    // Fetch new data based on the selected time frame
    // fetchData(newTimeFrame);
  };

  // Example of fetching data when time frame changes
  /*
  useEffect(() => {
    fetchData(timeFrame);
  }, [timeFrame]);

  const fetchData = (timeFrame: string) => {
    // Implement data fetching logic here
    // For example:
    // fetch(`/api/sentiment-over-time?timeFrame=${timeFrame}`)
    //   .then(response => response.json())
    //   .then(data => setData(data));
  };
  */
  const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined);

  interface RenderActiveShapeProps extends SectorProps {
    cx: number;
    cy: number;
    innerRadius: number;
    outerRadius: number;
    startAngle: number;
    endAngle: number;
    fill: string;
    payload: any;
    percent: number;
    value: number;
    index: number;
  }

  // Define the AnimatedSector component
  interface AnimatedSectorProps {
    className?: string;
    style?: React.CSSProperties;
    [key: string]: any;
  }

  const AnimatedSector: React.FC<AnimatedSectorProps> = ({ className, style, ...props }) => (
    <Sector {...props} className={className} style={style} />
  );

  const renderActiveShape = (props: any): JSX.Element => {
    const {
      cx,
      cy,
      innerRadius,
      outerRadius,
      startAngle,
      endAngle,
      fill,
    } = props as RenderActiveShapeProps;

    return (
      <g>
        <AnimatedSector
          cx={cx}
          cy={cy}
          innerRadius={innerRadius}
          outerRadius={outerRadius + 10}
          startAngle={startAngle}
          endAngle={endAngle}
          fill={adjustColorBrightness(fill, 20)}
          stroke={lumoDarkThemeColors.contrast10pct}
          strokeWidth={1}
          style={{ transition: 'all 0.3s ease-in-out', cursor: 'pointer' }}
        />
      </g>
    );
  };





  // Define the color variables
  const vaadinColors = {
    '--vaadin-charts-color-0': '#5ac2f7',
    '--vaadin-charts-color-1': '#1676f3',
    '--vaadin-charts-color-2': '#ff7d94',
    '--vaadin-charts-color-3': '#c5164e',
    '--vaadin-charts-color-4': '#15c15d',
    '--vaadin-charts-color-5': '#0e8151',
    '--vaadin-charts-color-6': '#c18ed2',
    '--vaadin-charts-color-7': '#9233b3',
    '--vaadin-charts-color-8': '#fda253',
    '--vaadin-charts-color-9': '#e24932',
    '--vaadin-charts-color-positive': '#15c15d',
    '--vaadin-charts-color-negative': '#e24932',
    '--vaadin-charts-axis-label': '#A3B4C2', // Adjust as per --lumo-secondary-text-color
    '--vaadin-charts-grid-line': '#1E252B',  // Adjust as per --lumo-contrast-20pct
  };
  // Extracted Lumo dark theme colors
  const lumoDarkThemeColors = {
    baseColor: 'hsl(214, 35%, 21%)', // --lumo-base-color
    headerTextColor: 'hsl(214, 100%, 98%)', // --lumo-header-text-color
    bodyTextColor: 'hsla(214, 96%, 96%, 0.9)', // --lumo-body-text-color
    secondaryTextColor: 'hsla(214, 87%, 92%, 0.69)', // --lumo-secondary-text-color
    contrast5pct: 'hsla(214, 65%, 85%, 0.06)', // --lumo-contrast-5pct
    contrast10pct: 'hsla(214, 60%, 80%, 0.14)', // --lumo-contrast-10pct
    contrast20pct: 'hsla(214, 64%, 82%, 0.23)', // --lumo-contrast-20pct
    contrast70pct: 'hsla(214, 87%, 92%, 0.69)', // --lumo-contrast-70pct
    primaryColor: 'hsl(214, 90%, 48%)', // --lumo-primary-color
    positiveColor: 'hsl(145, 72%, 30%)', // --lumo-success-color
    negativeColor: 'hsl(3, 79%, 49%)', // --lumo-error-color
    neutralColor: 'hsl(214, 90%, 48%)', // Using primary color for neutral
    tooltipBackgroundColor: 'hsl(214, 35%, 21%)',
    tooltipBorderColor: 'hsla(214, 60%, 80%, 0.14)',
    tooltipTextColor: 'hsla(214, 96%, 96%, 0.9)',
  };

  // Gradient definitions
  const gradients = [
    {
      id: 'positiveGradient',
      color: lumoDarkThemeColors.positiveColor,
    },
    {
      id: 'neutralGradient',
      color: lumoDarkThemeColors.neutralColor,
    },
    {
      id: 'negativeGradient',
      color: lumoDarkThemeColors.negativeColor,
    },
  ];


  return (
    <>
      <VerticalLayout style={{ alignItems: 'stretch', justifyContent: 'around' }} className="px-0 py-0 p-xl">
        <HorizontalLayout style={{ alignItems: 'stretch', justifyContent: 'around' }}>
          <div
            style={{
              borderBottom: '1px solid var(--lumo-contrast-10pct)',
              borderRight: '1px solid var(--lumo-contrast-10pct)',
              flex: 1,
            }}>
            <VerticalLayout className="p-l">
              <h2 className="font-normal m-0 text-secondary text-xs">Days active</h2>
              <span className="font-semibold text-3xl">21</span>

            </VerticalLayout>
          </div>
          <div
            style={{
              borderBottom: '1px solid var(--lumo-contrast-10pct)',
              borderRight: '1px solid var(--lumo-contrast-10pct)',
              flex: 1,
            }}>
            <VerticalLayout className="p-l">
              <h2 className="font-normal m-0 text-secondary text-xs">Total chats initiated</h2>
              <span className="font-semibold text-3xl">143</span>
              <span {...{ theme: 'badge success' }}>
                <Icon icon="vaadin:arrow-up" style={{ padding: '0.25em' }} className="box-border p-xs" />
                <span>+2%</span>
              </span>
            </VerticalLayout>
          </div>
          <div
            style={{
              borderBottom: '1px solid var(--lumo-contrast-10pct)',
              borderRight: '1px solid var(--lumo-contrast-10pct)',
              flex: 1,
            }}>
            <VerticalLayout className="p-l">
              <h2 className="font-normal m-0 text-secondary text-xs">Messages sent</h2>
              <span className="font-semibold text-3xl">198</span>
              <span {...{ theme: 'badge success' }}>
                <Icon icon="vaadin:arrow-up" style={{ padding: '0.25em' }} className="box-border p-xs" />
                <span>+1%</span>
              </span>
            </VerticalLayout>
          </div>
          <div
            style={{
              borderBottom: '1px solid var(--lumo-contrast-10pct)',
              borderRight: '1px solid var(--lumo-contrast-10pct)',
              flex: 1,
            }}>
            <VerticalLayout className="p-l">
              <h2 className="font-normal m-0 text-secondary text-xs">Assistant responses received</h2>
              <span className="font-semibold text-3xl">178</span>
              <span {...{ theme: 'badge' }}>
                <span>0%</span>
              </span>
            </VerticalLayout>
          </div>
        </HorizontalLayout>
        <VerticalLayout
          className="p-l full-w"
          style={{
            alignItems: 'stretch',
            justifyContent: 'stretch',
            borderRight: '1px solid var(--lumo-contrast-10pct)',
            borderBottom: '1px solid var(--lumo-contrast-10pct)',
            backgroundColor: 'var(--vaadin-charts-background)',
            fontFamily: 'var(--lumo-font-family)',
          }}>
          <HorizontalLayout
            style={{
              alignItems: 'stretch',
              justifyContent: 'between',
              borderRight: '1px solid var(--lumo-contrast-10pct)',
            }}>
            <VerticalLayout className="gap-m p-l px-xl self-end justify-start items-start flex-1">
              <h2 className="text-xl m-0">Messages over time</h2>
              <span className="text-secondary text-xs">
                An area chart displaying the number of messages over time (daily, weekly, or monthly)
              </span>
            </VerticalLayout>
            <VerticalLayout className="gap-m p-l px-l self-end justify-start items-start">
              <Select
                className="p-s self-center"
                items={selectItems}
                value={selectItems[0].value}
                onValueChanged={handleTimeFrameChange}></Select>
            </VerticalLayout>
          </HorizontalLayout>
          <ResponsiveContainer width="100%" height={400}>
            <AreaChart
              data={averageSentimentDailyData}
              margin={{
                top: 10,
                right: 30,
                left: 0,
                bottom: 0,
              }}>
              <defs>
                <linearGradient id="colorAverageSentiment" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="55%" stopColor={vaadinColors['--vaadin-charts-color-7']} stopOpacity={0.6} />
                  <stop offset="90%" stopColor={vaadinColors['--vaadin-charts-color-7']} stopOpacity={0.5} />
                </linearGradient>
              </defs>
              <XAxis
                dataKey="time"
                stroke={vaadinColors['--vaadin-charts-axis-label']}
                tickFormatter={(timeStr) => new Date(timeStr).toLocaleDateString()}
              />
              <YAxis
                stroke={vaadinColors['--vaadin-charts-axis-label']}
                domain={[0, 1]}
                tickFormatter={(value) => value.toFixed(1)}
              />
              <CartesianGrid stroke={vaadinColors['--vaadin-charts-grid-line']} strokeDasharray="3 3" />
              <Tooltip
                contentStyle={{
                  backgroundColor: lumoDarkThemeColors.tooltipBackgroundColor,
                  borderColor: lumoDarkThemeColors.tooltipBorderColor,
                  color: lumoDarkThemeColors.tooltipTextColor,
                  borderRadius: '4px',
                  borderStyle: 'solid',
                  borderWidth: '1px',
                }}
                itemStyle={{
                  color: lumoDarkThemeColors.bodyTextColor,
                }}
                cursor={{ stroke: lumoDarkThemeColors.contrast10pct }}
                labelFormatter={(label: string | number | Date) => `Date: ${new Date(label).toLocaleDateString()}`}
              />
              <Area
                type="monotone"
                dataKey="averageSentiment"
                stroke={vaadinColors['--vaadin-charts-color-7']}
                fillOpacity={1}
                fill="url(#colorAverageSentiment)"
                strokeWidth={3}
                dot={{ r: 1 }}
                activeDot={{ r: 8 }}
              />
            </AreaChart>
          </ResponsiveContainer>
        </VerticalLayout>

        <VerticalLayout
          className="p-l full-w"
          style={{
            alignItems: 'stretch',
            justifyContent: 'stretch',
            borderRight: '1px solid var(--lumo-contrast-10pct)',
            borderBottom: '1px solid var(--lumo-contrast-10pct)',
            backgroundColor: 'var(--vaadin-charts-background)',
            fontFamily: 'var(--lumo-font-family)',
          }}>
          <HorizontalLayout style={{ alignItems: 'stretch', justifyContent: 'between' }}>
            <VerticalLayout className="gap-m p-l px-l self-start justify-start items-start flex-1">
              <h2 className="text-xl m-0">Top Topics Bar Chart</h2>
              <span className="text-secondary text-xs">
                  A horizontal bar chart showcasing the most frequently mentioned topics in customer interactions.
                </span>
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={topTopicsData} layout="vertical" margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={lumoDarkThemeColors.contrast20pct} />
                <XAxis type="number" stroke={lumoDarkThemeColors.secondaryTextColor} />
                <YAxis dataKey="topic" type="category" stroke={lumoDarkThemeColors.secondaryTextColor} width={150} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: lumoDarkThemeColors.tooltipBackgroundColor,
                    borderColor: lumoDarkThemeColors.tooltipBorderColor,
                    color: lumoDarkThemeColors.tooltipTextColor,
                    borderRadius: '4px',
                    borderStyle: 'solid',
                    borderWidth: '1px',
                  }}
                />
                <Bar dataKey="count" fill={lumoDarkThemeColors.primaryColor} />
              </BarChart>
            </ResponsiveContainer>
            </VerticalLayout>
          </HorizontalLayout>
        </VerticalLayout>
      </VerticalLayout>
    </>
  );
}
