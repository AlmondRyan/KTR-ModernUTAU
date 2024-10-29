using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Xaml.Data;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Navigation;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;

// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace KTR_ModernUTAU
{
    public class Track
    {
        public string TrackName { get; set; }
        public ObservableCollection<string> Patterns { get; set; } = new ObservableCollection<string>();
    }
    public sealed partial class TrackEditor : UserControl
    {
        private ObservableCollection<Track> tracks = new ObservableCollection<Track>();
        public TrackEditor()
        {
            this.InitializeComponent();
        }

        private void AddTrack_Click(object sender, RoutedEventArgs e)
        {
            var newTrack = new Track { TrackName = $"Track {tracks.Count + 1}" };
            tracks.Add(newTrack);
        }

        private void AddPattern_Click(object sender, RoutedEventArgs e)
        {
            var button = sender as Button;
            var track = button.DataContext as Track;

            if (track != null)
            {
                track.Patterns.Add($"Pattern {track.Patterns.Count + 1}");
            }
        }
    }
}
